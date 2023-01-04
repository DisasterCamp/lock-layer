package com.disaster.locklayer.domain.share;

import com.disaster.locklayer.infrastructure.constant.Constants;

public class LockEntity {
    private String key;
    private Integer expireTime = Constants.KEY_EXPIRE;
    private Boolean isReentryLock = false;
    private String _key;
    private Thread currentThread = Thread.currentThread();

    public static LockEntity build() {
        return new LockEntity();
    }

    public String getKey() {
        return key;
    }

    public LockEntity setKey(String key) {
        this.key = key;
        this._key = Constants.KEY_PREFIX + this.key;
        return this;
    }

    public Integer getExpireTime() {
        return expireTime;
    }

    public LockEntity setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public Boolean getReentryLock() {
        return isReentryLock;
    }

    public LockEntity setReentryLock(Boolean reentryLock) {
        isReentryLock = reentryLock;
        return this;
    }

    public String get_key() {
        return _key;
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(Thread currentThread) {
        this.currentThread = currentThread;
    }
}
