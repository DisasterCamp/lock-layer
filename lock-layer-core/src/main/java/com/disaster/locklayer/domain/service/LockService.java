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
     * @param lockManager                          the lock manager
     * @param lock                                 the lock
     */
    void allocFuture( LockManager lockManager, LockEntity lock);

    /**
     * Retry lock.
     *
     * @param lockManager        the lock manager
     * @param lock               the lock
     */
    boolean retryLock(LockManager lockManager, LockEntity lock);

    /**
     * Unlock.
     *
     * @param lockManager        the lock manager
     * @param lockTimerEntityMap the lock timer entity map
     * @param key                the key
     */
    void unlock(LockManager lockManager,ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap,String key);
}
