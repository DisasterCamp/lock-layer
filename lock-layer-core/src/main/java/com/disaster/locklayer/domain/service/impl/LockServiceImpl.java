package com.disaster.locklayer.domain.service.impl;

import com.disaster.locklayer.domain.redis.JedisClient;
import com.disaster.locklayer.domain.service.LockService;
import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.constant.Constants;
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

    private Logger log = LoggerFactory.getLogger(LockServiceImpl.class);

    @Override
    public void allocFuture(ScheduledExecutorService executorService, ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, LockManager lockManager, LockEntity lockEntity) {
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            if (Objects.nonNull(jedis(lockManager).get(lockEntity.get_key()))) {
                LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(lockEntity.get_key());
                if (lockHeartBeatEntity.getExpireCount().get() < 3) {
                    jedis(lockManager).expire(lockEntity.get_key(), lockEntity.getExpireTime());
                    lockHeartBeatEntity.setLockTime(SystemClock.now());
                    lockHeartBeatEntity.addAndGetExpireCount(1);
                } else {
                    Thread.currentThread().interrupt();
                }
            } else {
                Thread.currentThread().interrupt();
            }
        }, LockManager.calculationPeriod(lockEntity.getExpireTime()), LockManager.calculationPeriod(lockEntity.getExpireTime()), TimeUnit.SECONDS);
        lockTimerEntityMap.put(lockEntity.get_key(), lockEntity.getReentryLock() ? LockHeartBeatEntity.build().setFuture(scheduledFuture).addAndGetReentryCount(1) : LockHeartBeatEntity.build().setFuture(scheduledFuture));
    }

    @Override
    public boolean retryLock(ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, LockManager lockManager, LockEntity lockEntity
    ) {
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(lockEntity.get_key());
        if (lockEntity.getReentryLock() && lockHeartBeatEntity.isCurrentThread()) {
            lockTimerEntityMap.get(lockEntity.get_key()).addAndGetReentryCount(1);
            if (log.isDebugEnabled()) {
                log.info("key = {},reentryCount +1 ,currentReentryCount = {}", lockEntity.getKey(), lockHeartBeatEntity.getReentryCount());
            } else {
                System.out.println(String.format("key = %s,reentryCount +1,currentReentryCount = %s", lockEntity.getKey(), lockHeartBeatEntity.getReentryCount()));
            }
            return true;
        } else {
            long now = SystemClock.now();
            AtomicBoolean isRetryLock = new AtomicBoolean(false);
            Executors.newSingleThreadExecutor().execute(() -> {
                for (; ; ) {
                    if ((SystemClock.now() - now) >= Constants.KEY_EXPIRE) {
                        if (log.isDebugEnabled()) {
                            log.info("key = {},retry lock fail , Record request", lockEntity.getKey());
                        } else {
                            System.out.println(String.format("key = {},retry lock fail , Record request", lockEntity.getKey()));
                        }
                        ConcurrentHashMap<String, LockEntity> retryLockMap = retryLockMap(lockManager);
                        retryLockMap.put(lockEntity.get_key(), lockEntity);
                        break;
                    }
                    Object result = jedis(lockManager).eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(), lockEntity.getExpireTime().toString()));
                    if (result.equals(Constants.LUA_RES_OK)) {
                        isRetryLock.set(true);
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            while (!isRetryLock.get()) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void unlock(LockManager lockManager, ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, String key) {
        String _key = Constants.KEY_PREFIX + key;
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(_key);
        if (Objects.isNull(lockHeartBeatEntity)) {
            if (log.isDebugEnabled()) {
                log.info("Lock does not exist！");
            } else {
                System.out.println("Lock does not exist！");
            }
            return;
        }
        if (lockHeartBeatEntity.isCurrentThread()) {
            Object result;
            if (lockHeartBeatEntity.getReentryCount().get() > 1) {
                lockHeartBeatEntity.decrementAndGetReentryCount();
                if (log.isDebugEnabled()) {
                    log.info("key = {},reentryCount -1 ,currentReentryCount = {}", key, lockHeartBeatEntity.getReentryCount());
                } else {
                    System.out.println(String.format("key = %s,reentryCount -1,currentReentryCount = %s", key, lockHeartBeatEntity.getReentryCount()));
                }
            } else {
                result = jedis(lockManager).eval(LuaUtils.getUnLockLuaStr(), Collections.singletonList(_key), Collections.singletonList(key));
                lockHeartBeatEntity.decrementAndGetReentryCount();
                lockHeartBeatEntity.shutdown();
                lockTimerEntityMap.remove(_key);
                if (result.equals(1l)) {
                    if (log.isDebugEnabled()) {
                        log.info("key = {},unlock success", key);
                    } else {
                        System.out.println(String.format("key = %s ,unlock success", key));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.info("key = {},not exist", key);
                    } else {
                        System.out.println(String.format("key = {},not exist", key));
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.info("current Thread can't unlock other thread key");
            } else {
                System.out.println("current Thread can't unlock other thread key");
            }
        }
    }

    private JedisClient jedis(LockManager lockManager) {
        return lockManager.getLockConfig().getClient();
    }

    private ConcurrentHashMap<String, LockEntity> retryLockMap(LockManager lockManager) {
        return lockManager.getRetryLockMap();
    }


}
