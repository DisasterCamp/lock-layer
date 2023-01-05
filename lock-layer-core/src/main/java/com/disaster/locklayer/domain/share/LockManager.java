package com.disaster.locklayer.domain.share;

import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.disaster.locklayer.infrastructure.annotations.Lock;
import com.disaster.locklayer.infrastructure.config.LockConfig;
import com.disaster.locklayer.infrastructure.constant.Constants;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Lock manager.
 *
 * @author disaster
 * @version 1.0
 */
public class LockManager {
    private Logger log = LoggerFactory.getLogger(LockManager.class);
    /**
     * Configuration class for locks
     */
    private LockConfig lockConfig;

    /**
     * Annotated collection of methods
     */
    private Set<Method> methodSet;

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
                    if ((value.getExpireCount().get() >= Constants.MAX_EXPIRE_COUNT * (value.getReentryCount().get() > 0 ? value.getReentryCount().get() : 1))
                            || (value.getLockTime() - SystemClock.now() >= Constants.MAX_EXPIRE_TIME)) {
                        if (Objects.isNull(value.getFuture())) throw new RuntimeException("future is null");
                        Boolean shutdown = value.shutdown();
                        if (shutdown) {
                            LoggerUtil.printlnLog(LockManager.class,String.format("key = %s,If the number of consecutive times is exceeded, the lock is released", next.getKey()));
                            iterator.remove();
                        } else {
                            throw new RuntimeException(value.getFuture().isDone() + "RetryLockMonitorThread is");
                        }
                    }
                }
            }
        });
    }

    /**
     * Create lock manager.
     *
     * @return the lock manager
     */
    public static LockManager create() {
        LockManager lockManager = new LockManager();
        lockManager.setLockConfig(LockConfig.build());
        Reflections reflections = lockManager.getLockConfig().getReflections();
        lockManager.setMethodSet(reflections.getMethodsAnnotatedWith(Lock.class));
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
        Reflections reflections = lockManager.getLockConfig().getReflections();
        lockManager.setMethodSet(reflections.getMethodsAnnotatedWith(Lock.class));
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
     * Gets method set.
     *
     * @return the method set
     */
    public Set<Method> getMethodSet() {
        return methodSet;
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
     * Sets method set.
     *
     * @param methodSet the method set
     */
    public void setMethodSet(Set<Method> methodSet) {
        this.methodSet = methodSet;
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
