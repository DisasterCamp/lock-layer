package com.disaster.locklayer.domain.service.impl;

import com.disaster.locklayer.domain.redis.JedisClient;
import com.disaster.locklayer.domain.service.LockService;
import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.constant.Constants;
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
            if (Objects.nonNull(jedis(lockManager).get(lockEntity.get_key()))) {
                LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(lockEntity.get_key());
                if (lockHeartBeatEntity.getExpireCount().get() < 3) {
                    jedis(lockManager).expire(lockEntity.get_key(), lockEntity.getExpireTime());
                    lockHeartBeatEntity.setLockTime(SystemClock.now());
                    lockHeartBeatEntity.addAndGetExpireCount(1);
                    LoggerUtil.printlnLog(this.getClass(), String.format("key = %s continuance success"));
                } else {
                    Thread.currentThread().interrupt();
                }
            } else {
                Thread.currentThread().interrupt();
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
        countDownLatch.await();
        LockHeartBeatEntity lockHeartBeat = lockEntity.getReentryLock() ? LockHeartBeatEntity.build().setFuture(scheduledFuture).addAndGetReentryCount(1) : LockHeartBeatEntity.build().setFuture(scheduledFuture);
        lockTimerEntityMap.put(lockEntity.get_key(), lockHeartBeat);
    }

    @Override
    public boolean retryLock(ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, LockManager lockManager, LockEntity lockEntity
    ) {
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(lockEntity.get_key());
        if (lockEntity.getReentryLock() && lockHeartBeatEntity.isCurrentThread()) {
            lockTimerEntityMap.get(lockEntity.get_key()).addAndGetReentryCount(1);
            LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,reentryCount +1,currentReentryCount = %s", lockEntity.getKey(), lockHeartBeatEntity.getReentryCount()));
            return true;
        } else {
            long now = SystemClock.now();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            AtomicBoolean isLock = new AtomicBoolean(false);
            new Thread(() -> {
                while ((SystemClock.now() - now) < Constants.MAX_RETRY_TIME) {
                    Object result = jedis(lockManager).eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(), lockEntity.getExpireTime().toString()));
                    if (result.equals(Constants.LUA_RES_OK)) {
                        LoggerUtil.printlnLog(this.getClass(), String.format("thread = %s ,key = %s,retry lock success", Thread.currentThread().getName(), lockEntity.getKey()));
                        countDownLatch.countDown();
                        isLock.getAndSet(true);
                        break;
                    }
                }
                if (!isLock.get()) {
                    LoggerUtil.printlnLog(this.getClass(), String.format("thread = %s ,key = %s,retry lock fail , Record request", Thread.currentThread().getName(), lockEntity.getKey()));
                    ConcurrentHashMap<String, LockEntity> retryLockMap = retryLockMap(lockManager);
                    retryLockMap.put(lockEntity.get_key(), lockEntity);
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
            if (lockHeartBeatEntity.getReentryCount().get() > 1) {
                lockHeartBeatEntity.decrementAndGetReentryCount();
                LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,reentryCount -1,currentReentryCount = %s", key, lockHeartBeatEntity.getReentryCount()));
            } else {
                result = jedis(lockManager).eval(LuaUtils.getUnLockLuaStr(), Collections.singletonList(_key), Collections.singletonList(key));
                lockHeartBeatEntity.decrementAndGetReentryCount();
                lockHeartBeatEntity.shutdown();
                lockTimerEntityMap.remove(_key);
                if (result.equals(1l)) {
                    LoggerUtil.printlnLog(this.getClass(), String.format("key = %s ,unlock success", key));
                } else {
                    LoggerUtil.printlnLog(this.getClass(), String.format("key = %s,not exist", key));
                }
            }
        } else {
            LoggerUtil.printlnLog(this.getClass(), "current Thread can't unlock other thread key");
        }
    }

    private JedisClient jedis(LockManager lockManager) {
        return lockManager.getLockConfig().getClient();
    }

    private ConcurrentHashMap<String, LockEntity> retryLockMap(LockManager lockManager) {
        return lockManager.getRetryLockMap();
    }


}
