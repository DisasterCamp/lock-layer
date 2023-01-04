package com.disaster.locklayer.domain.redis;

import com.disaster.locklayer.domain.service.LockService;
import com.disaster.locklayer.domain.service.impl.LockServiceImpl;
import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.infrastructure.constant.Constants;
import com.disaster.locklayer.infrastructure.persistence.LockLayer;
import com.disaster.locklayer.infrastructure.utils.LuaUtils;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * The type Jedis lock.
 *
 * @author disaster
 * @version 1.0
 */
public class RedisLockLayerLayer implements LockLayer {
    private LockManager lockManager;

    private LockService lockService = new LockServiceImpl();

    private Logger log = LoggerFactory.getLogger(RedisLockLayerLayer.class);


    /**
     * Instantiates a new Jedis lock.
     *
     * @param lockManager the lock manager
     */
    public RedisLockLayerLayer(LockManager lockManager) {
        this.lockManager = lockManager;
    }


    @Override
    public boolean tryLock(String key) {
        if (StringUtils.isBlank(key)) new NullPointerException("key can't empty or null");
        Object result = _lock(LockEntity.build().setKey(key));
        return result.equals(Constants.LUA_RES_OK);
    }

    @Override
    public boolean tryLock(String key, Integer expireTime) {
        if (StringUtils.isBlank(key)) new NullPointerException("key can't empty or null");
        Object result = _lock(LockEntity.build().setKey(key).setExpireTime(expireTime));
        return result.equals(Constants.LUA_RES_OK);
    }


    @Override
    public void unLock(String key) {
        if (StringUtils.isBlank(key)) new NullPointerException("key can't empty or null");
        lockService.unlock(lockManager, lockTimerEntityMap(), key);
    }

    @Override
    public boolean tryReentryLock(String key) {
        if (StringUtils.isBlank(key)) new NullPointerException("key can't empty or null");
        Object result = _lock(LockEntity.build().setKey(key).setReentryLock(true));
        return result.equals(Constants.LUA_RES_OK);
    }

    @Override
    public boolean tryReentryLock(String key, Integer expireTime) {
        if (StringUtils.isBlank(key)) new NullPointerException("key can't empty or null");
        Object result = _lock(LockEntity.build().setKey(key).setReentryLock(true));
        return result.equals(Constants.LUA_RES_OK);
    }


    private Object _lock(LockEntity lockEntity) {
        Object result = jedis().eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(),lockEntity.getExpireTime().toString()));
        if (result.equals(Constants.LUA_RES_OK)) {
            //heatBeat
            lockService.allocFuture(executorService(), lockTimerEntityMap(), lockManager, lockEntity);
        } else {
            //retryLock
            lockService.retryLock(lockTimerEntityMap(), lockManager, lockEntity);
        }
        return result;
    }

    private JedisClient jedis() {
        return lockManager.getLockConfig().getClient();
    }

    private ConcurrentHashMap<String, LockHeartBeatEntity> lockTimerEntityMap() {
        return lockManager.getLockTimerEntityConcurrentHashMap();
    }

    private ScheduledExecutorService executorService() {
        return lockManager.getLockConfig().getSchedule();
    }
}
