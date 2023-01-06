package com.disaster.locklayer.domain.service;

import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.domain.share.LockManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The interface Lock service.
 *
 * @author disaster
 * @version 1.0
 */
public interface LockService {
    /**
     * Alloc future.
     *
     * @param scheduledExecutorService             the scheduled executor service
     * @param lockHeartBeatEntityConcurrentHashMap the lock heart beat entity concurrent hash map
     * @param lockManager                          the lock manager
     * @param lock                                 the lock
     */
    void allocFuture(ScheduledExecutorService scheduledExecutorService, ConcurrentHashMap<String, LockHeartBeatEntity> lockHeartBeatEntityConcurrentHashMap, LockManager lockManager, LockEntity lock);

    /**
     * Retry lock.
     *
     * @param lockTimerEntityMap the lock timer entity map
     * @param lockManager        the lock manager
     * @param lock               the lock
     */
    boolean retryLock(ScheduledExecutorService executorService,ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap, LockManager lockManager, LockEntity lock);

    /**
     * Unlock.
     *
     * @param lockManager        the lock manager
     * @param lockTimerEntityMap the lock timer entity map
     * @param key                the key
     */
    void unlock(LockManager lockManager,ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap,String key);
}
