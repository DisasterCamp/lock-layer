package com.disaster.locklayer;

import com.disaster.locklayer.domain.Lock;
import com.disaster.locklayer.domain.service.impl.LockServiceImpl;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.persistence.LockLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class LockLayerApplication {
    @Autowired
    private LockLayer redisLockLayer;
    @Autowired
    private LockManager lockManager;


    public static void main(String[] args) {
        SpringApplication.run(LockLayerApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner1() {
        return args -> {
            for (int i = 0; i < 10; i++) {
                new Thread(()->{
                    redisLockLayer.tryLock("key");
                }).start();
            }
//            lock.lock();
//            lock.lockException();
        };
    }

    //    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
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
        };
    }
}
