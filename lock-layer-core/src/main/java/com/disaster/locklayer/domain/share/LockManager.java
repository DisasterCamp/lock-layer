package com.disaster.locklayer.domain.share;

import com.disaster.locklayer.infrastructure.utils.LockConfigUtil;
import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.disaster.locklayer.infrastructure.config.LockConfig;
import com.disaster.locklayer.infrastructure.constant.Constants;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    public static Double period = 3d / 4d;

    /**
     * Thread container
     */
    private volatile ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * The retry lock queue in the process
     */
    private volatile ConcurrentHashMap<String, LockEntity> retryLockMap = new ConcurrentHashMap<>();

    /**
     * Listen for the number of locks
     */
    private static ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("RetryLockMonitorThread")
            .build());


    /**
     * Instantiates a new Lock manager.
     */
    public LockManager() {
        executorService.execute(() -> {
            while (true) {
                Set<Map.Entry<String, LockHeartBeatEntity>> entries = lockTimerEntityConcurrentHashMap.entrySet();
                Iterator<Map.Entry<String, LockHeartBeatEntity>> iterator = entries.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, LockHeartBeatEntity> next = iterator.next();
                    LockHeartBeatEntity value = next.getValue();
                    if ((value.getExpireCount().get() >= LockConfigUtil.getMaxExpireCount() * getAnInt(value))
                            || (value.getLockTime() - SystemClock.now() >= (LockConfigUtil.getMaxExpireTime() * (getAnInt(value))))) {
                        LoggerUtil.printlnLog(Thread.currentThread().getClass(), String.format("key = %s, exclude max time,begin release", next.getKey()));
                        if (Objects.isNull(value.getFuture())) throw new RuntimeException("future is null");
                        Boolean shutdown = value.shutdown();
                        if (shutdown) {
                            LoggerUtil.printlnLog(Thread.currentThread().getClass(), String.format("key = %s,If the number of consecutive times is exceeded, the lock is released", next.getKey()));
                            iterator.remove();
                        } else {
                            throw new RuntimeException(value.getFuture().isDone() + "RetryLockMonitorThread is");
                        }
                    }
                }
            }
        });
    }

    private int getAnInt(LockHeartBeatEntity value) {
        return value.getReentryCount().get() > 1 ? value.getReentryCount().get() : 1;
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
}
