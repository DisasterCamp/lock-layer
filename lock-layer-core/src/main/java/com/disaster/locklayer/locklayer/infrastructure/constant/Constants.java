package com.disaster.locklayer.locklayer.infrastructure.constant;


/**
 * The interface Constants.
 *
 * @author disaster
 * @version 1.0
 */
public interface Constants {
    /**
     * Default lock key
     */
    String DEFAULT_KEY = "lock:layer:default_key";

    /**
     * Default expiration time of the lock
     */
    Integer KEY_EXPIRE = 30;

    /**
     * Maximum retry time
     */
    Integer MAX_RETRY_TIME = 30 * 1000;

    /**
     * Maximum duration of lock renewal
     */
    Long MAX_EXPIRE_TIME = 60l;

    /**
     * Maximum number of locks to be renewed
     */
    Integer MAX_EXPIRE_COUNT = 3;

    /**
     * Lock prefix
     */
    String KEY_PREFIX = "lock:layer:prefix:";

    /**
     * Lock prefix
     */
    String CHANNEL_PREFIX = "lock:layer:channel:prefix:";

    /**
     * Locked return value
     */
    String RES_OK = "OK";

    /**
     * lua lock return value
     */
    Long LUA_RES_OK = 1l;


    /**
     * Lock retry subscription consumption channel
     */
    String UNLOCK_CHANNEL = "unlock_channel";

    /**
     * configuration prefix
     */
    String CONFIG_PREFIX = "lock.layer.declaration";


}
