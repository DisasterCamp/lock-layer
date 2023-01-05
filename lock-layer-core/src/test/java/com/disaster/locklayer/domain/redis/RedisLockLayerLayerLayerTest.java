package com.disaster.locklayer.domain.redis;

import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.config.LockConfig;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

public class RedisLockLayerLayerLayerTest {
    private LockManager lockManager;
    private RedisLockLayerLayer redisLockLayer;

    @Before
    public void init() {
        lockManager = LockManager.create(LockConfig.build().setClient(new JedisClient(new JedisPool("139.196.154.217", 6379, null, "123456"))));
        redisLockLayer = new RedisLockLayerLayer(lockManager);
    }

    @Test
    public void lockLayerTest() throws InterruptedException {
        boolean test_key1 = redisLockLayer.tryLock("test_key");
        boolean test_key = redisLockLayer.tryLock("test_key");
        System.out.println("test_key = " + test_key);
        System.out.println("test_key1 = " + test_key1);
        Thread.sleep(10000);
        redisLockLayer.unLock("test_key");
        Thread.sleep(50000);
    }


    @Test
    public void diffThreadLockTest() throws InterruptedException {
        Thread thread = new Thread(() -> {
            redisLockLayer.tryLock("test_key");
            try {
                Thread.sleep(10000);
                redisLockLayer.unLock("test_key");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"thread1");
        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisLockLayer.tryLock("test_key");
        },"thread2");
        thread.start();
        thread1.start();
        while (true) {

        }
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
