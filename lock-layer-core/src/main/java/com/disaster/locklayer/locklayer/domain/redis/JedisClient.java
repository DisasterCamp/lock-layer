package com.disaster.locklayer.locklayer.domain.redis;

import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;

import java.util.List;

/**
 * The type Jedis client.
 */
public class JedisClient {
    private JedisPool jedisPool;

    /**
     * Instantiates a new Jedis client.
     *
     * @param jedisPool the jedis pool
     */
    public JedisClient(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * Expire long.
     *
     * @param key     the key
     * @param seconds the seconds
     * @return the long
     */
    public long expire(String key, long seconds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.expire(key, seconds);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * String get command
     *
     * @param key the key
     * @return string string
     */
    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * String set command
     *
     * @param key   the key
     * @param value the value
     * @param time  the time
     * @return string string
     */
    public String set(String key, String value, long time) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.set(key, value, SetParams.setParams().nx().px(time));

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * Pub long.
     *
     * @param channel the channel
     * @param msg     the msg
     * @return the long
     */
    public long pub(String channel, String msg) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.publish(channel, msg);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * Eval lua script command
     *
     * @param script the script
     * @param keys   the keys
     * @param args   the args
     * @return object object
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.eval(script, keys, args);

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * Subscribe.
     *
     * @param jedisPubSub the jedis pub sub
     * @param channels    the channels
     */
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.subscribe(jedisPubSub, channels);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Publish.
     *
     * @param channel the channel
     * @param message the message
     */
    public void publish(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.publish(channel, message);
            LoggerUtil.println(JedisClient.class, "Channel:" + channel + "  Message:" + message);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * String delete command
     *
     * @param key the key
     * @return long long
     */
    public Long del(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(key);

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Gets jedis pool.
     *
     * @return the jedis pool
     */
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * Sets jedis pool.
     *
     * @param jedisPool the jedis pool
     */
    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
