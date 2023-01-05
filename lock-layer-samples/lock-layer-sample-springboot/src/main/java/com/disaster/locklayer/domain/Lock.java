package com.disaster.locklayer.domain;

import com.disaster.locklayer.infrastructure.annotations.LockLayer;
import org.springframework.stereotype.Component;

@Component
public class Lock {

    @LockLayer(key = "test_key", expireTime = 100)
    public void lock() {

    }

    @LockLayer(key = "test_key", expireTime = 100, reentryLock = true)
    public void retryLock() {

    }

    @LockLayer(key = "test_key", expireTime = 10)
    public void lockException() {
        throw new RuntimeException();
    }
}
