package com.disaster.locklayer.domain.share;

import com.disaster.locklayer.infrastructure.constant.Constants;
import com.disaster.locklayer.infrastructure.utils.MacUtil;
import sun.misc.Contended;

/**
 * The type Lock entity. set jvm param -XX:-RestrictContended ，It can effectively improve the concurrency performance
 *
 * @author disaster
 * @version 1.0
 */
public class LockEntity {
    private String key;
    private int expireTime = Constants.KEY_EXPIRE;
    private boolean isReentryLock = false;
    private String _key;
    private volatile String threadIdentification = MacUtil.getCurrentIpLocalMac() + ":" + Thread.currentThread().getId();


    /**
     * Build lock entity.
     *
     * @return the lock entity
     */
    public static LockEntity build() {
        return new LockEntity();
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
     * @return the key
     */
    public LockEntity setKey(String key) {
        this.key = key;
        this._key = Constants.KEY_PREFIX + this.key;
        return this;
    }

    /**
     * Gets expire time.
     *
     * @return the expire time
     */
    public Integer getExpireTime() {
        return expireTime;
    }

    /**
     * Sets expire time.
     *
     * @param expireTime the expire time
     * @return the expire time
     */
    public LockEntity setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    /**
     * Gets reentry lock.
     *
     * @return the reentry lock
     */
    public Boolean getReentryLock() {
        return isReentryLock;
    }

    /**
     * Sets reentry lock.
     *
     * @param reentryLock the reentry lock
     * @return the reentry lock
     */
    public LockEntity setReentryLock(Boolean reentryLock) {
        isReentryLock = reentryLock;
        return this;
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public String get_key() {
        return _key;
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
    public LockEntity setThreadIdentification(String threadIdentification) {
        this.threadIdentification = threadIdentification;
        return this;
    }

}
