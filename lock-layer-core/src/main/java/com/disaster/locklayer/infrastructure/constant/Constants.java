package com.disaster.locklayer.infrastructure.constant;


/**
 * The interface Constants.
 *
 * @author disaster
 * @version 1.0
 */
public interface Constants {
    /**
     * 默认锁key
     */
    String DEFAULT_KEY = "lock:layer:default_key";

    /**
     * 锁的默认过期时间
     */
    Integer KEY_EXPIRE = 30;

    /**
     * 续锁最长时间
     */
    Long MAX_EXPIRE_TIME = 60l;

    /**
     * 最大续锁次数
     */
    Integer MAX_EXPIRE_COUNT = 3;

    /**
     * 锁前缀
     */
    String KEY_PREFIX = "lock:layer:prefix:";



    /**
     * 加锁返回值
     */
    String RES_OK = "OK";

    /**
     * lua加锁返回值
     */
    Long LUA_RES_OK = 1l;

    /**
     * 锁重试的订阅/消费通道
     */
    String UNLOCK_CHANNEL = "unlock_channel";


}
