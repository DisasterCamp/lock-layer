package com.disaster.locklayer.locklayer.infrastructure.config;

import com.disaster.locklayer.domain.redis.JedisClient;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * The type Lock config.
 *
 * @author disaster
 * @version 1.0
 */
public class LockConfig {
    private JedisClient client;
    private ScheduledExecutorService schedule;

    /**
     * Instantiates a new Lock config.
     */
    public LockConfig() {
        this.client = new JedisClient(new JedisPool());
        this.schedule = new ScheduledThreadPoolExecutor(2 * Runtime.getRuntime().availableProcessors() + 1,
                new BasicThreadFactory.Builder().namingPattern("redisLock-schedule-pool-%d").daemon(false).build());
    }

    /**
     * Build lock config.
     *
     * @return the lock config
     */
    public static LockConfig build() {
        return new LockConfig();
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public JedisClient getClient() {
        return client;
    }

    /**
     * Sets client.
     *
     * @param client the client
     * @return the client
     */
    public LockConfig setClient(JedisClient client) {
        this.client = client;
        return this;
    }

    /**
     * Gets schedule.
     *
     * @return the schedule
     */
    public ScheduledExecutorService getSchedule() {
        return schedule;
    }

    /**
     * Sets schedule.
     *
     * @param schedule the schedule
     * @return the schedule
     */
    public LockConfig setSchedule(ScheduledExecutorService schedule) {
        this.schedule = schedule;
        return this;
    }
}
