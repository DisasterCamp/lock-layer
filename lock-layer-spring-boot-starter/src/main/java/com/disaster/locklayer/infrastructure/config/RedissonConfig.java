package com.disaster.locklayer.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + "139.196.154.217:6379");
        singleServerConfig.setDatabase(1);
        singleServerConfig.setPassword("123456");
        return Redisson.create(config);
    }

    @Bean
    public ApplicationRunner applicationRunner(){
        return args -> {
            RedissonClient client = redissonClient();
            RLock lock = client.getLock("lock_test");
            lock.lock();
        };
    }
}
