package com.disaster.locklayer.domain.share;

import com.disaster.locklayer.infrastructure.utils.MacUtil;
import com.disaster.locklayer.infrastructure.utils.SystemClock;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Lock heart beat entity.
 *
 * @author disaster
 * @version 1.0
 */
public class LockHeartBeatEntity {
    /**
     * Scheduled task
     */
    private ScheduledFuture<?> future;
    /**
     * continuance
     */
    private volatile AtomicInteger expireCount = new AtomicInteger(0);

    /**
     * The number of times elapsed
     */
    private volatile AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * threadIdentification
     */
    private volatile String threadIdentification = MacUtil.getCurrentIpLocalMac() + ":" + Thread.currentThread().getId();

    /**
     * reentry count
     */
    private volatile AtomicInteger reentryCount = new AtomicInteger(0);

    /**
     * first lock time
     */
    private volatile Long lockTime = SystemClock.now();


    /**
     * Build lock heart beat entity.
     *
     * @return the lock heart beat entity
     */
    public static LockHeartBeatEntity build() {
        return new LockHeartBeatEntity();
    }

    /**
     * Gets future.
     *
     * @return the future
     */
    public ScheduledFuture<?> getFuture() {
        return future;
    }

    /**
     * Sets future.
     *
     * @param future the future
     * @return the future
     */
    public LockHeartBeatEntity setFuture(ScheduledFuture<?> future) {
        this.future = future;
        return this;
    }

    /**
     * Shutdown lock heart beat entity.
     *
     * @return the lock heart beat entity
     */
    public Boolean shutdown() {
        if (future != null && !future.isDone())
            future.cancel(true);
        return future.isCancelled();
    }

    /**
     * Gets expire count.
     *
     * @return the expire count
     */
    public AtomicInteger getExpireCount() {
        return expireCount;
    }

    /**
     * Gets and add.
     *
     * @param expireCount the expire count
     * @return the and add
     */
    public LockHeartBeatEntity addAndGetExpireCount(int expireCount) {
        this.expireCount.addAndGet(expireCount);
        return this;
    }

    /**
     * Gets and add reentry count.
     *
     * @param reentryCount the reentry count
     * @return the and add reentry count
     */
    public LockHeartBeatEntity addAndGetReentryCount(int reentryCount) {
        this.reentryCount.addAndGet(reentryCount);
        return this;
    }

    /**
     * Decrement and get reentry count lock heart beat entity.
     *
     * @return the lock heart beat entity
     */
    public LockHeartBeatEntity decrementAndGetReentryCount() {
        this.reentryCount.decrementAndGet();
        return this;
    }

    /**
     * Is current thread boolean.
     *
     * @return the boolean
     */
    public Boolean isCurrentThread() {
        return this.threadIdentification.equals(MacUtil.getCurrentIpLocalMac() + ":" + Thread.currentThread().getId());
    }

    /**
     * Gets shutdown.
     *
     * @return the shutdown
     */
    public AtomicBoolean getShutdown() {
        return shutdown;
    }

    /**
     * Sets shutdown.
     *
     * @param shutdown the shutdown
     * @return the shutdown
     */
    public LockHeartBeatEntity setShutdown(AtomicBoolean shutdown) {
        this.shutdown = shutdown;
        return this;
    }


    /**
     * Gets lock time.
     *
     * @return the lock time
     */
    public Long getLockTime() {
        return lockTime;
    }

    /**
     * Sets lock time.
     *
     * @param lockTime the lock time
     * @return the lock time
     */
    public LockHeartBeatEntity setLockTime(Long lockTime) {
        this.lockTime = lockTime;
        return this;
    }

    /**
     * Gets reentry count.
     *
     * @return the reentry count
     */
    public AtomicInteger getReentryCount() {
        return reentryCount;
    }

    /**
     * Gets thread identification.
     *
     * @return the thread identification
     */
    public String getThreadIdentification() {
        return threadIdentification;
    }

    /**
     * Sets thread identification.
     *
     * @param threadIdentification the thread identification
     */
    public LockHeartBeatEntity setThreadIdentification(String threadIdentification) {
        this.threadIdentification = threadIdentification;
        return this;
    }
}
