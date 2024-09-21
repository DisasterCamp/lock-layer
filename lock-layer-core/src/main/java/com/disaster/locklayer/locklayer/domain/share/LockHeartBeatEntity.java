package com.disaster.locklayer.locklayer.domain.share;

import com.disaster.locklayer.infrastructure.utils.MacUtil;
import com.disaster.locklayer.infrastructure.utils.SystemClock;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * The type Lock heart beat entity.if set jvm param -XX:-RestrictContended ，It can effectively improve the concurrency performance
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
    private volatile LongAdder expireCount = new LongAdder();

    /**
     * lock key
     */
    private String key;

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
    private volatile LongAdder reentryCount = new LongAdder();


    /**
     * first lock time
     */
    private volatile long lockTime = SystemClock.now();


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
    public LongAdder getExpireCount() {
        return expireCount;
    }

    /**
     * Gets and add.
     *
     * @param expireCount the expire count
     * @return the and add
     */
    public LockHeartBeatEntity addAndGetExpireCount(int expireCount) {
        this.expireCount.add(expireCount);
        return this;
    }

    /**
     * Gets and add reentry count.
     *
     * @param reentryCount the reentry count
     * @return the and add reentry count
     */
    public LockHeartBeatEntity addAndGetReentryCount(int reentryCount) {
        this.reentryCount.add(reentryCount);
        return this;
    }

    /**
     * Decrement and get reentry count lock heart beat entity.
     *
     * @return the lock heart beat entity
     */
    public LockHeartBeatEntity decrementAndGetReentryCount() {
        this.reentryCount.decrement();
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
    public LongAdder getReentryCount() {
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
     * @return the thread identification
     */
    public LockHeartBeatEntity setThreadIdentification(String threadIdentification) {
        this.threadIdentification = threadIdentification;
        return this;
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets key.
     *
     * @param key the key
     */
    public LockHeartBeatEntity setKey(String key) {
        this.key = key;
        return this;
    }
}
