package com.disaster.locklayer.domain.service.impl;

import com.disaster.locklayer.domain.redis.JedisClient;
import com.disaster.locklayer.domain.service.LockService;
import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.constant.Constants;
import com.disaster.locklayer.infrastructure.utils.LockConfigUtil;
import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import com.disaster.locklayer.infrastructure.utils.LuaUtils;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * The type Lock service.
 */
public class LockServiceImpl implements LockService {


    @Override
    @SneakyThrows
    public void allocFuture(ScheduledExecutorService executorService, ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, LockManager lockManager, LockEntity lockEntity) {
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            Thread thread = Thread.currentThread();
            if (Objects.nonNull(jedis(lockManager).get(lockEntity.get_key()))) {
                LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(lockEntity.get_key());
                jedis(lockManager).expire(lockEntity.get_key(), lockEntity.getExpireTime());
                lockHeartBeatEntity.addAndGetExpireCount(1);
                LoggerUtil.printlnLog(thread.getClass(), String.format("key = %s continuance success", lockEntity.get_key()));
            }
        }, LockManager.calculationPeriod(lockEntity.getExpireTime()), LockManager.calculationPeriod(lockEntity.getExpireTime()), TimeUnit.SECONDS);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long now = SystemClock.now();
        while (true) {
            if (Objects.nonNull(scheduledFuture) || SystemClock.now() - now > 10 * 1000) {
                countDownLatch.countDown();
                break;
            }
        }
        LockHeartBeatEntity lockHeartBeat = lockEntity.getReentryLock() ? LockHeartBeatEntity.build().setFuture(scheduledFuture).addAndGetReentryCount(1) : LockHeartBeatEntity.build().setFuture(scheduledFuture);
        lockTimerEntityMap.put(lockEntity.get_key(), lockHeartBeat);
        countDownLatch.await();
    }

    @Override
    public boolean retryLock(ScheduledExecutorService executorService, ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, LockManager lockManager, LockEntity lockEntity
    ) {
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(lockEntity.get_key());
        if (lockEntity.getReentryLock() && lockHeartBeatEntity.isCurrentThread()) {
            if (lockTimerEntityMap.get(lockEntity.get_key()).getReentryCount().intValue() < LockConfigUtil.getMaxReentryCount()) {
                lockTimerEntityMap.get(lockEntity.get_key()).addAndGetReentryCount(1);
                LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,reentryCount +1,currentReentryCount = %s", lockEntity.getKey(), lockHeartBeatEntity.getReentryCount()));
                return true;
            } else {
                LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,thread = ,Maximum reentry times exceeded ,currentReentryCount = %s", lockEntity.getKey(), Thread.currentThread().getId(), lockHeartBeatEntity.getReentryCount()));
                return false;
            }
        } else {
            long now = SystemClock.now();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            AtomicBoolean isLock = new AtomicBoolean(false);
            new Thread(() -> {
                Long retryTime;
                while ((retryTime = SystemClock.now() - now) < LockConfigUtil.getMaxRetryTime()) {
                    Object result = jedis(lockManager).eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(), lockEntity.getExpireTime().toString()));
                    if (result.equals(Constants.LUA_RES_OK)) {
                        LoggerUtil.printlnLog(this.getClass(), String.format("thread = %s ,key = %s,retry lock success , retry time = %d ms", Thread.currentThread().getName(), lockEntity.get_key(), retryTime));
                        countDownLatch.countDown();
                        isLock.getAndSet(true);
                        allocFuture(executorService, lockTimerEntityMap, lockManager, lockEntity);
                        break;
                    }
                }
                if (!isLock.get()) {
                    LoggerUtil.printlnLog(this.getClass(), String.format("thread = %s ,key = %s,retry lock fail , Record request", Thread.currentThread().getName(), lockEntity.get_key()));
                    ConcurrentHashMap<String, LockEntity> retryLockMap = retryLockMap(lockManager);
                    retryLockMap.put(lockEntity.get_key(), lockEntity);
                    lockManager.handlerFailLockProcessor(lockEntity);
                    countDownLatch.countDown();
                }
            }).start();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return isLock.get();
        }
    }

    @Override
    public void unlock(LockManager lockManager, ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, String key) {
        String _key = Constants.KEY_PREFIX + key;
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(_key);
        if (Objects.isNull(lockHeartBeatEntity)) {
            LoggerUtil.printlnLog(this.getClass(), "Lock does not exist！");
            return;
        }
        if (lockHeartBeatEntity.isCurrentThread()) {
            Object result;
            if (lockHeartBeatEntity.getReentryCount().intValue() > 1) {
                lockHeartBeatEntity.decrementAndGetReentryCount();
                LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,reentryCount -1,currentReentryCount = %s", key, lockHeartBeatEntity.getReentryCount()));
            } else {
                result = jedis(lockManager).eval(LuaUtils.getUnLockLuaStr(), Collections.singletonList(_key), Collections.singletonList(key));
                lockHeartBeatEntity.decrementAndGetReentryCount();
                lockHeartBeatEntity.shutdown();
                lockTimerEntityMap.remove(_key);
                if (result.equals(Constants.LUA_RES_OK)) {
                    LoggerUtil.printlnLog(this.getClass(), String.format("key = %s ,unlock success", _key));
                } else {
                    LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,not exist", _key));
                }
            }
        } else {
            LoggerUtil.printlnLog(this.getClass(), "current thread can't unlock other thread lock");
            throw new RuntimeException("current thread can't unlock other thread lock");
        }
    }

    private JedisClient jedis(LockManager lockManager) {
        return lockManager.getLockConfig().getClient();
    }

    private ConcurrentHashMap<String, LockEntity> retryLockMap(LockManager lockManager) {
        return lockManager.getRetryLockMap();
    }


}
