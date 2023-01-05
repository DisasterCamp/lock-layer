package com.disaster.locklayer;

import com.disaster.locklayer.domain.redis.JedisClient;
import com.disaster.locklayer.domain.redis.RedisLockLayerLayer;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.config.LockConfig;
import lombok.SneakyThrows;
import redis.clients.jedis.JedisPool;

/**
 * The type Main.
 *
 * @author disaster
 * @version 1.0
 */
public class Main {
    private static LockManager lockManager;
    private static RedisLockLayerLayer redisLockLayer;


    static {
        lockManager = LockManager.create(LockConfig.build().setClient(new JedisClient(new JedisPool("127.0.0.1", 6379, null, "123456"))));
        redisLockLayer = new RedisLockLayerLayer(lockManager);
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
//        lockLayerTest();
        diffThreadLockTest();
//        lockSingleTest();
//        retryLockLayerTest();
    }

    /**
     * Lock layer test.
     */
    @SneakyThrows
    public static void lockLayerTest() {
        boolean test_key1 = redisLockLayer.tryLock("test_key");
        boolean test_key = redisLockLayer.tryLock("test_key");
        System.out.println("test_key = " + test_key);
        System.out.println("test_key1 = " + test_key1);
        Thread.sleep(10000);
        redisLockLayer.unLock("test_key");
        Thread.sleep(50000);
    }

    /**
     * Diff thread lock test.
     */
    public static void diffThreadLockTest() {
        Thread thread = new Thread(() -> {
            redisLockLayer.tryLock("test_key");
            try {
                Thread.sleep(10000);
                redisLockLayer.unLock("test_key");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "thread1");
        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisLockLayer.tryLock("test_key");
        }, "thread2");
        thread.start();
        thread1.start();
        while (true) {

        }
    }

    /**
     * Lock single test.
     */
    @SneakyThrows
    public static void lockSingleTest() {
        redisLockLayer.tryLock("test_key");
        Thread.sleep(50000);
    }

    /**
     * Retry lock layer test.
     */
    @SneakyThrows
    public static void retryLockLayerTest() {
        redisLockLayer.tryReentryLock("test_key");
        redisLockLayer.tryReentryLock("test_key");
        Thread.sleep(50000);
        redisLockLayer.unLock("test_key");
        Thread.sleep(50000);
    }
}
