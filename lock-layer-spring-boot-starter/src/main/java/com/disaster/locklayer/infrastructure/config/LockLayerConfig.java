package com.disaster.locklayer.infrastructure.config;

import com.disaster.locklayer.domain.redis.JedisClient;
import com.disaster.locklayer.domain.redis.RedisLockLayerLayer;
import com.disaster.locklayer.domain.service.LockHeatProcessor;
import com.disaster.locklayer.domain.service.LockProcessor;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.persistence.LockLayer;
import com.disaster.locklayer.infrastructure.prop.LockLayerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The type Lock layer config.
 *
 * @author disaster
 * @version 1.0
 */
@Configuration
@EnableAspectJAutoProxy
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@EnableConfigurationProperties(LockLayerProperties.class)
public class LockLayerConfig {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Jedis client jedis client.
     *
     * @return the jedis client
     */
    @Bean
    @ConditionalOnMissingBean(RedisProperties.class)
    public JedisClient jedisClient(RedisProperties redisProperties) {
        JedisClient jedisClient = null;
        try {
            JedisPool bean = applicationContext.getBean(JedisPool.class);
            jedisClient = new JedisClient(bean);
        } catch (BeansException e) {
            RedisProperties.Jedis jedis = redisProperties.getJedis();
            RedisProperties.Pool pool = jedis.getPool();
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(pool.getMaxActive());
            jedisPoolConfig.setMaxIdle(pool.getMaxIdle());
            jedisPoolConfig.setMinIdle(pool.getMinIdle());
            jedisPoolConfig.setMaxWait(pool.getMaxWait());
            JedisPool lockLayerJedisPool = new JedisPool(jedisPoolConfig, redisProperties.getHost(), redisProperties.getPort(), Objects.isNull(redisProperties.getTimeout()) ? -1 : Integer.valueOf(String.valueOf(redisProperties.getTimeout().toMinutes())), redisProperties.getPassword());
            jedisClient = new JedisClient(lockLayerJedisPool);
        }
        return jedisClient;
    }

    /**
     * Lock config lock config.
     *
     * @param jedisClient the jedis client
     * @return the lock config
     */
    @Bean
    @ConditionalOnBean(JedisClient.class)
    public LockConfig lockConfig(JedisClient jedisClient) {
        LockConfig lockConfig = LockConfig.build().setClient(jedisClient);
        return lockConfig;
    }

    /**
     * Lock manager lock manager.
     *
     * @param lockConfig the lock config
     * @return the lock manager
     */
    @Bean
    @ConditionalOnBean(LockConfig.class)
    public LockManager lockManager(LockConfig lockConfig) {
        LockManager lockManager = LockManager.create(lockConfig);
        Map<String, LockProcessor> lockProcessorMap = applicationContext.getBeansOfType(LockProcessor.class);
        Map<String, LockHeatProcessor> lockHeatProcessorMap = applicationContext.getBeansOfType(LockHeatProcessor.class);
        List<LockProcessor> lockProcessors = new ArrayList<>(lockProcessorMap.values());
        List<LockHeatProcessor> lockHeatProcessors = new ArrayList<>(lockHeatProcessorMap.values());
        lockManager.setLockHeatProcessorList(lockHeatProcessors);
        lockManager.setLockProcessorList(lockProcessors);
        return lockManager;
    }

    /**
     * Lock layer lock layer.
     *
     * @param lockManager the lock manager
     * @return the lock layer
     */
    @Bean
    @ConditionalOnBean(LockManager.class)
    public LockLayer lockLayer(LockManager lockManager) {
        log.info("========= lock layer init success!! ========");
        return new RedisLockLayerLayer(lockManager);
    }


}
