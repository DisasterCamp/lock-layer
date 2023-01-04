package com.disaster.locklayer.infrastructure.config;

import com.disaster.locklayer.domain.redis.JedisClient;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ConfigurationBuilder;
import redis.clients.jedis.Jedis;
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
    private Reflections reflections;
    private ScheduledExecutorService schedule;

    /**
     * Instantiates a new Lock config.
     */
    public LockConfig() {
        this.client = new JedisClient(new JedisPool());
        this.reflections = new Reflections(new ConfigurationBuilder()
                .forPackages("com.disaster.locklayer")
                .addScanners(new SubTypesScanner())
                .addScanners(new MethodAnnotationsScanner())
                .addScanners(new MethodParameterScanner())
                .addScanners(new TypeAnnotationsScanner())
                .addScanners(new TypeElementsScanner()));
        this.schedule = new ScheduledThreadPoolExecutor(2 * Runtime.getRuntime().availableProcessors() + 1,
                new BasicThreadFactory.Builder().namingPattern("redisLock-schedule-pool-%d").daemon(true).build());
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
     * Gets reflections.
     *
     * @return the reflections
     */
    public Reflections getReflections() {
        return reflections;
    }

    /**
     * Sets reflections.
     *
     * @param reflections the reflections
     * @return the reflections
     */
    public LockConfig setReflections(Reflections reflections) {
        this.reflections = reflections;
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
