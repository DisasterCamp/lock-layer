package com.disaster.locklayer.locklayer.domain.service.impl;

import com.disaster.locklayer.domain.redis.JedisClient;
import com.disaster.locklayer.domain.service.LockService;
import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.constant.Constants;
import com.disaster.locklayer.infrastructure.enums.LogLevel;
import com.disaster.locklayer.infrastructure.utils.LockConfigUtil;
import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import com.disaster.locklayer.infrastructure.utils.LuaUtils;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisPubSub;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Lock service.
 */
public class LockServiceImpl implements LockService {


    @Override
    @SneakyThrows
    public void allocFuture(LockManager lockManager, LockEntity lockEntity) {
        ScheduledFuture<?> scheduledFuture = executorService(lockManager).scheduleAtFixedRate(() -> {
            Thread thread = Thread.currentThread();
            if (Objects.nonNull(jedis(lockManager).get(lockEntity.get_key()))) {
                LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap(lockManager).get(lockEntity.get_key());
                jedis(lockManager).expire(lockEntity.get_key(), lockEntity.getExpireTime());
                lockHeartBeatEntity.addAndGetExpireCount(1);
                LoggerUtil.println(thread.getClass(), String.format("key = %s continuance success", lockEntity.get_key()));
            }
        }, LockManager.calculationPeriod(lockEntity.getExpireTime()), LockManager.calculationPeriod(lockEntity.getExpireTime()), TimeUnit.SECONDS);
        LockHeartBeatEntity lockHeartBeat = lockEntity.getReentryLock() ? LockHeartBeatEntity.build().setKey(lockEntity.getKey()).setFuture(scheduledFuture).addAndGetReentryCount(1) : LockHeartBeatEntity.build().setKey(lockEntity.getKey()).setFuture(scheduledFuture);
        lockTimerEntityMap(lockManager).put(lockEntity.get_key(), lockHeartBeat);
    }

    @Override
    public boolean retryLock(LockManager lockManager, LockEntity lockEntity) {
        ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityConcurrentHashMap = lockManager.getLockTimerEntityConcurrentHashMap();
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityConcurrentHashMap.get(lockEntity.get_key());
        if (lockEntity.getReentryLock() && lockHeartBeatEntity.isCurrentThread()) {
            if (lockTimerEntityConcurrentHashMap.get(lockEntity.get_key()).getReentryCount().intValue() < LockConfigUtil.getMaxReentryCount()) {
                lockTimerEntityConcurrentHashMap.get(lockEntity.get_key()).addAndGetReentryCount(1);
                LoggerUtil.println(this.getClass(), String.format("key = %s,reentryCount +1,currentReentryCount = %s", lockEntity.getKey(), lockHeartBeatEntity.getReentryCount()));
                return true;
            } else {
                LoggerUtil.println(this.getClass(), String.format("key = %s,thread = %s,Maximum reentry times exceeded ,currentReentryCount = %s", lockEntity.getKey(), Thread.currentThread().getId(), lockHeartBeatEntity.getReentryCount()));
                return false;
            }
        } else {
            long now = SystemClock.now();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            AtomicBoolean isLock = new AtomicBoolean(false);
            if (LockConfigUtil.getRenewType().equals("redis")) {
                RenewLockHandler renewLockHandler = new RenewLockHandler(countDownLatch, isLock, lockManager, lockEntity, this);
                final RenewLockHandler finalRenewLockHandler = renewLockHandler;
                LoggerUtil.println(this.getClass(), "channel = " + Constants.CHANNEL_PREFIX + lockEntity.getKey() + " begin subscribe");
                jedis(lockManager).subscribe(finalRenewLockHandler, Constants.CHANNEL_PREFIX + lockEntity.getKey());
                try {
                    countDownLatch.await(LockConfigUtil.getMaxRetryTime(), TimeUnit.MICROSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    LoggerUtil.println(this.getClass(), "channel = " + Constants.CHANNEL_PREFIX + lockEntity.getKey() + " begin unsubscribe");
                    finalRenewLockHandler.unsubscribe(Constants.CHANNEL_PREFIX + lockEntity.getKey());
                    //help GC
                    renewLockHandler = null;
                }
            } else {
                getRetryExecutorThread(lockManager).submit(new RetryLockHandler(this, countDownLatch, lockManager, lockEntity, now, isLock));
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return isLock.get();
        }
    }

    @Override
    public void unlock(LockManager lockManager, ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, String key) {
        String _key = Constants.KEY_PREFIX + key;
        LockHeartBeatEntity lockHeartBeatEntity = lockTimerEntityMap.get(_key);
        if (Objects.isNull(lockHeartBeatEntity)) {
            LoggerUtil.println(this.getClass(), "Lock does not exist！");
            return;
        }
        if (lockHeartBeatEntity.isCurrentThread()) {
            Object result;
            if (lockHeartBeatEntity.getReentryCount().intValue() > 1) {
                lockHeartBeatEntity.decrementAndGetReentryCount();
                LoggerUtil.println(this.getClass(), String.format("key = %s,reentryCount -1,currentReentryCount = %s", key, lockHeartBeatEntity.getReentryCount()));
            } else {
                result = jedis(lockManager).eval(LuaUtils.getUnLockLuaStr(), Collections.singletonList(_key), Collections.singletonList(key));
                lockHeartBeatEntity.decrementAndGetReentryCount();
                lockHeartBeatEntity.shutdown();
                lockTimerEntityMap.remove(_key);
                if (result.equals(Constants.LUA_RES_OK)) {
                    if (LockConfigUtil.getRenewType().equals("redis")) {
                        jedis(lockManager).publish(Constants.CHANNEL_PREFIX + lockHeartBeatEntity.getKey(), "unregister");
                    }
                    LoggerUtil.println(this.getClass(), String.format("key = %s ,unlock success", _key));
                } else {
                    LoggerUtil.println(this.getClass(), String.format("key = %s,not exist", _key),LogLevel.ERROR);
                }
            }
        } else {
            LoggerUtil.println(this.getClass(), "current thread can't unlock other thread lock", LogLevel.WARN);
            throw new IllegalMonitorStateException("current thread can't unlock other thread lock");
        }
    }

    private JedisClient jedis(LockManager lockManager) {
        return lockManager.getLockConfig().getClient();
    }

    private ConcurrentHashMap<String, LockEntity> retryLockMap(LockManager lockManager) {
        return lockManager.getRetryLockMap();
    }

    private ExecutorService getRetryExecutorThread(LockManager lockManager) {
        return lockManager.getRetryLockExecutorService();
    }

    private ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap(LockManager lockManager) {
        return lockManager.getLockTimerEntityConcurrentHashMap();
    }

    private ScheduledExecutorService executorService(LockManager lockManager) {
        return lockManager.getLockConfig().getSchedule();
    }


    public static class RenewLockHandler extends JedisPubSub {
        private CountDownLatch countDownLatch;
        private AtomicBoolean isLock;
        private LockManager lockManager;
        private LockEntity lockEntity;
        private LockServiceImpl lockService;


        public RenewLockHandler(CountDownLatch countDownLatch, AtomicBoolean isLock, LockManager lockManager, LockEntity lockEntity, LockServiceImpl lockService) {
            this.countDownLatch = countDownLatch;
            this.isLock = isLock;
            this.lockManager = lockManager;
            this.lockEntity = lockEntity;
            this.lockService = lockService;
        }

        private JedisClient jedis(LockManager lockManager) {
            return lockManager.getLockConfig().getClient();
        }

        private ConcurrentHashMap<String, LockEntity> retryLockMap(LockManager lockManager) {
            return lockManager.getRetryLockMap();
        }

        @Override
        public void proceed(Connection client, String... channels) {
            super.proceed(client, channels);
        }

        @Override
        public boolean isSubscribed() {
            return super.isSubscribed();
        }

        @Override
        public void ping() {
            super.ping();
        }

        @Override
        public int getSubscribedChannels() {
            return super.getSubscribedChannels();
        }

        @Override
        public void onMessage(String channel, String message) {
            if (channel.equals(Constants.CHANNEL_PREFIX + lockEntity.getKey()) && message.equals("unregister")) {
                Object result = jedis(lockManager).eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(), lockEntity.getExpireTime().toString()));
                if (result.equals(Constants.LUA_RES_OK)) {
                    LoggerUtil.println(this.getClass(), String.format("pubsub = %s ,key = %s,retry lock success", this.getClass().getSimpleName(), lockEntity.get_key()));
                    isLock.getAndSet(true);
                    countDownLatch.countDown();
                    lockService.allocFuture(lockManager, lockEntity);
                    unsubscribe(Constants.CHANNEL_PREFIX + lockEntity.getKey());
                }
            }
        }
    }


    static class RetryLockHandler implements Runnable {
        private LockServiceImpl lockService;
        private CountDownLatch countDownLatch;
        private LockManager lockManager;
        private LockEntity lockEntity;
        private long now;
        private AtomicBoolean isLock;

        /**
         * Instantiates a new Retry lock handler.
         *
         * @param lockService    the lock service
         * @param countDownLatch the count down latch
         * @param lockManager    the lock manager
         * @param lockEntity     the lock entity
         * @param now            the now
         */
        public RetryLockHandler(LockServiceImpl lockService, CountDownLatch countDownLatch, LockManager lockManager, LockEntity lockEntity, long now, AtomicBoolean isLock) {
            this.lockService = lockService;
            this.countDownLatch = countDownLatch;
            this.lockManager = lockManager;
            this.lockEntity = lockEntity;
            this.now = now;
            this.isLock = isLock;
        }


        private JedisClient jedis(LockManager lockManager) {
            return lockManager.getLockConfig().getClient();
        }

        private ConcurrentHashMap<String, LockEntity> retryLockMap(LockManager lockManager) {
            return lockManager.getRetryLockMap();
        }

        @Override
        public void run() {
            Long retryTime;
            while ((retryTime = SystemClock.now() - now) < LockConfigUtil.getMaxRetryTime()) {
                Object result = jedis(lockManager).eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(), lockEntity.getExpireTime().toString()));
                if (result.equals(Constants.LUA_RES_OK)) {
                    LoggerUtil.println(this.getClass(), String.format("thread = %s ,key = %s,retry lock success , retry time = %d ms", Thread.currentThread().getName(), lockEntity.get_key(), retryTime));
                    countDownLatch.countDown();
                    isLock.getAndSet(true);
                    lockService.allocFuture(lockManager, lockEntity);
                    break;
                }
            }
            if (!isLock.get()) {
                LoggerUtil.println(this.getClass(), String.format("thread = %s ,key = %s,retry lock fail , Record request", Thread.currentThread().getName(), lockEntity.get_key()),LogLevel.ERROR);
                ConcurrentHashMap<String, LockEntity> retryLockMap = retryLockMap(lockManager);
                retryLockMap.put(lockEntity.get_key(), lockEntity);
                lockManager.handlerFailLockProcessor(lockEntity);
                countDownLatch.countDown();
            }
        }
    }


}
