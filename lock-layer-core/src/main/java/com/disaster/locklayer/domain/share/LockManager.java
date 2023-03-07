package com.disaster.locklayer.domain.share;

import com.disaster.locklayer.domain.service.LockHeatProcessor;
import com.disaster.locklayer.domain.service.LockProcessor;
import com.disaster.locklayer.domain.service.impl.LockServiceImpl;
import com.disaster.locklayer.infrastructure.utils.LockConfigUtil;
import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.disaster.locklayer.infrastructure.config.LockConfig;
import com.disaster.locklayer.infrastructure.constant.Constants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Lock manager.
 *
 * @author disaster
 * @version 1.0
 */
public class LockManager {
    /**
     * Configuration class for locks
     */
    private LockConfig lockConfig;

    /**
     * The constant period.
     */
    public static Double period = 1d / 2d;

    /**
     * Thread container
     */
    private volatile ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * The retry lock queue in the process
     */
    private volatile ConcurrentHashMap<String, LockEntity> retryLockMap = new ConcurrentHashMap<>();

    /**
     * lock processor container
     */
    private volatile List<LockProcessor> lockProcessorList = new ArrayList<>();

    /**
     * lockHeat processor container
     */
    private volatile List<LockHeatProcessor> lockHeatProcessorList = new ArrayList<>();

    /**
     * Listen for the number of locks
     */
    private static ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("RetryLockMonitorThread")
            .build());

    /**
     * retry Lock Thread Pool
     */
    private static ExecutorService retryLockExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, new ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("retryLockThreadPool")
            .build());


    /**
     * Instantiates a new Lock manager.
     */
    public LockManager() {
        executorService.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Set<Map.Entry<String, LockHeartBeatEntity>> entries = lockTimerEntityConcurrentHashMap.entrySet();
                Iterator<Map.Entry<String, LockHeartBeatEntity>> iterator = entries.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, LockHeartBeatEntity> next = iterator.next();
                    LockHeartBeatEntity value = next.getValue();
                    if ((value.getExpireCount().intValue() >= LockConfigUtil.getMaxExpireCount() * getAnInt(value))
                            || (value.getLockTime() - SystemClock.now() >= (LockConfigUtil.getMaxExpireTime() * (getAnInt(value))))) {
                        LoggerUtil.printlnLog(Thread.currentThread().getClass(), String.format("key = %s, exclude max time,begin release", next.getKey()));
                        if (Objects.isNull(value.getFuture())) throw new RuntimeException("future is null");
                        Boolean shutdown = value.shutdown();
                        if (shutdown) {
                            if (LockConfigUtil.getRenewType().equals("redis")) {
                                lockConfig.getClient().publish(Constants.CHANNEL_PREFIX + value.getKey(), "unregister");
                            }
                            LoggerUtil.printlnLog(Thread.currentThread().getClass(), String.format("key = %s,If the number of consecutive times is exceeded, the lock is released", next.getKey()));
                            iterator.remove();
                            handlerLockHeartRemovedProcessor(value);
                        } else {
                            LoggerUtil.printlnLog(Thread.currentThread().getClass(), String.format("ScheduledFuture = %s,isn't cancel", next.getValue().getFuture()));
                            throw new RuntimeException(value.getFuture().isDone() + "RetryLockMonitorThread is");
                        }
                    }
                }
            }
        });
    }

    private int getAnInt(LockHeartBeatEntity value) {
        return value.getReentryCount().intValue() > 1 ? value.getReentryCount().intValue() : 1;
    }

    /**
     * Create lock manager.
     *
     * @return the lock manager
     */
    public static LockManager create() {
        LockManager lockManager = new LockManager();
        lockManager.setLockConfig(LockConfig.build());
        return lockManager;
    }

    /**
     * Create lock manager.
     *
     * @param lockConfig the lock config
     * @return the lock manager
     */
    public static LockManager create(LockConfig lockConfig) {
        LockManager lockManager = new LockManager();
        lockManager.setLockConfig(lockConfig);
        return lockManager;
    }


    /**
     * Gets lock config.
     *
     * @return the lock config
     */
    public LockConfig getLockConfig() {
        return lockConfig;
    }


    /**
     * Sets lock config.
     *
     * @param lockConfig the lock config
     */
    public void setLockConfig(LockConfig lockConfig) {
        this.lockConfig = lockConfig;
    }


    /**
     * Gets lock timer entity concurrent hash map.
     *
     * @return the lock timer entity concurrent hash map
     */
    public ConcurrentHashMap<String, LockHeartBeatEntity> getLockTimerEntityConcurrentHashMap() {
        return lockTimerEntityConcurrentHashMap;
    }

    /**
     * Gets lock timer entity.
     *
     * @param key the key
     * @return the lock timer entity
     */
    public LockHeartBeatEntity getLockTimerEntity(String key) {
        return lockTimerEntityConcurrentHashMap.get(Constants.KEY_PREFIX + key);
    }

    /**
     * Calculation period long.
     *
     * @param expireTime the expire time
     * @return the long
     */
    public static Long calculationPeriod(Integer expireTime) {
        Double result = Math.floor(period * expireTime);
        return result.longValue();
    }

    /**
     * Gets retry lock queue.
     *
     * @return the retry lock queue
     */
    public ConcurrentHashMap<String, LockEntity> getRetryLockMap() {
        return retryLockMap;
    }

    /**
     * Gets lock processor list.
     *
     * @return the lock processor list
     */
    public List<LockProcessor> getLockProcessorList() {
        return lockProcessorList;
    }

    /**
     * Sets lock processor list.
     *
     * @param lockProcessorList the lock processor list
     */
    public void setLockProcessorList(List<LockProcessor> lockProcessorList) {
        this.lockProcessorList = lockProcessorList;
    }

    /**
     * Gets lock heat processor list.
     *
     * @return the lock heat processor list
     */
    public List<LockHeatProcessor> getLockHeatProcessorList() {
        return lockHeatProcessorList;
    }

    /**
     * Sets lock heat processor list.
     *
     * @param lockHeatProcessorList the lock heat processor list
     */
    public void setLockHeatProcessorList(List<LockHeatProcessor> lockHeatProcessorList) {
        this.lockHeatProcessorList = lockHeatProcessorList;
    }


    /**
     * Gets retry lock executor service.
     *
     * @return the retry lock executor service
     */
    public ExecutorService getRetryLockExecutorService() {
        return retryLockExecutorService;
    }

    /**
     * Sets retry lock executor service.
     *
     * @param retryLockExecutorService the retry lock executor service
     */
    public void setRetryLockExecutorService(ExecutorService retryLockExecutorService) {
        LockManager.retryLockExecutorService = retryLockExecutorService;
    }

    /**
     * Handler fail lock processor.
     *
     * @param lockEntity the lock entity
     */
    public void handlerFailLockProcessor(LockEntity lockEntity) {
        if (Objects.nonNull(this.lockProcessorList) && !this.lockProcessorList.isEmpty()) {
            for (LockProcessor lockProcessor : lockProcessorList) {
                lockProcessor.failLockProcessor(lockEntity);
            }
        }
    }


    /**
     * Handler success lock processor.
     *
     * @param lockEntity the lock entity
     */
    public void handlerSuccessLockProcessor(LockEntity lockEntity) {
        if (Objects.nonNull(this.lockProcessorList) && !this.lockProcessorList.isEmpty()) {
            for (LockProcessor lockProcessor : lockProcessorList) {
                lockProcessor.successLockProcessor(lockEntity);
            }
        }
    }

    /**
     * Handler lock heart removed processor.
     *
     * @param value the value
     */
    public void handlerLockHeartRemovedProcessor(LockHeartBeatEntity value) {
        if (Objects.nonNull(this.lockHeatProcessorList) && !this.lockHeatProcessorList.isEmpty()) {
            for (LockHeatProcessor lockHeatProcessor : lockHeatProcessorList) {
                lockHeatProcessor.lockHeartRemovedProcessor(value);
            }
        }
    }
}
