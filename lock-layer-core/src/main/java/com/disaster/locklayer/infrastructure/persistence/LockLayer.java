package com.disaster.locklayer.infrastructure.persistence;

/**
 * The interface Lock.
 *
 * @author disaster
 * @version 1.0
 */
public interface LockLayer {
    /**
     * Try lock boolean.
     *
     * @param key the key
     * @return the boolean
     * @throws Exception the exception
     */
    boolean tryLock(String key);

    /**
     * Try lock boolean.
     *
     * @param key        the key
     * @param expireTime the expire time
     * @return the boolean
     * @throws Exception the exception
     */
    boolean tryLock(String key, Integer expireTime) ;

    /**
     * Un lock.
     *
     * @param key the key
     * @throws Exception the exception
     */
    void unLock(String key) ;


    /**
     * Try reentry lock boolean.
     *
     * @param key the key
     * @return the boolean
     */
    boolean tryReentryLock(String key);


    /**
     * Try reentry lock boolean.
     *
     * @param key        the key
     * @param expireTime the expire time
     * @return the boolean
     */
    boolean tryReentryLock(String key,Integer expireTime);
}
