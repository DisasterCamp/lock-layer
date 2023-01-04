package com.disaster.locklayer.domain.redis;

import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.config.LockConfig;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

public class RedisLockLayerLayerTest {
    private LockManager lockManager;
    private RedisLockLayerLayer redisLockLayer;

    @Before
    public void init() {
        lockManager = LockManager.create(LockConfig.build().setClient(new JedisClient(new JedisPool("139.196.154.217", 6379, null, "123456"))));
        redisLockLayer = new RedisLockLayerLayer(lockManager);
    }

    @Test
    public void lockLayerTest() throws InterruptedException {
        redisLockLayer.tryLock("test_key");
        redisLockLayer.tryLock("test_key");
        Thread.sleep(50000);
    }

    @Test
    public void lockSingleTest() throws InterruptedException {
        redisLockLayer.tryLock("test_key");
        Thread.sleep(50000);
    }

    @Test
    public void retryLockLayerTest() throws InterruptedException {
        redisLockLayer.tryReentryLock("test_key");
        redisLockLayer.tryReentryLock("test_key");
        Thread.sleep(50000);
        redisLockLayer.unLock("test_key");
        Thread.sleep(50000);
    }

    @Test
    public void timeTest() {
        System.out.println(Math.floor(LockManager.period * 30));
    }
}
