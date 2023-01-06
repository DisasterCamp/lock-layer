package com.disaster.locklayer.domain.redis;

import com.disaster.locklayer.domain.service.LockService;
import com.disaster.locklayer.domain.service.impl.LockServiceImpl;
import com.disaster.locklayer.domain.share.LockEntity;
import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import com.disaster.locklayer.infrastructure.asset.Assert;
import com.disaster.locklayer.infrastructure.constant.Constants;
import com.disaster.locklayer.infrastructure.persistence.LockLayer;
import com.disaster.locklayer.infrastructure.utils.LoggerUtil;
import com.disaster.locklayer.infrastructure.utils.LuaUtils;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * The type Jedis lock.
 *
 * @author disaster
 * @version 1.0
 */
public class RedisLockLayerLayer implements LockLayer {
    private LockManager lockManager;

    private LockService lockService;


    /**
     * Instantiates a new Jedis lock.
     *
     * @param lockManager the lock manager
     */
    public RedisLockLayerLayer(LockManager lockManager) {
        this.lockManager = lockManager;
        this.lockService = new LockServiceImpl();
    }


    @Override
    public boolean tryLock(String key) {
        Assert.AssertStrIsEmpty(key);
        Object result = _lock(LockEntity.build().setKey(key));
        return result.equals(Constants.LUA_RES_OK);
    }

    @Override
    public boolean tryLock(String key, Integer expireTime) {
        Assert.AssertStrIsEmpty(key);
        LockEntity lockEntity = LockEntity.build().setKey(key);
        if (expireTime > 0) lockEntity.setExpireTime(expireTime);
        Object result = _lock(lockEntity);
        return result.equals(Constants.LUA_RES_OK);
    }


    @Override
    public void unLock(String key) {
        Assert.AssertStrIsEmpty(key);
        lockService.unlock(lockManager, lockTimerEntityMap(), key);
    }

    @Override
    public boolean tryReentryLock(String key) {
        Assert.AssertStrIsEmpty(key);
        Object result = _lock(LockEntity.build().setKey(key).setReentryLock(true));
        return result.equals(Constants.LUA_RES_OK);
    }

    @Override
    public boolean tryReentryLock(String key, Integer expireTime) {
        Assert.AssertStrIsEmpty(key);
        LockEntity lockEntity = LockEntity.build().setKey(key).setReentryLock(true);
        if (expireTime > 0) lockEntity.setExpireTime(expireTime);
        Object result = _lock(lockEntity);
        return result.equals(Constants.LUA_RES_OK);
    }


    private Object _lock(LockEntity lockEntity) {
        Object result = jedis().eval(LuaUtils.getLockLuaStr(), Collections.singletonList(lockEntity.get_key()), Lists.newArrayList(lockEntity.getKey(), lockEntity.getExpireTime().toString()));
        if (result.equals(Constants.LUA_RES_OK)) {
            //heatBeat
            lockService.allocFuture(executorService(), lockTimerEntityMap(), lockManager, lockEntity);
            LoggerUtil.printlnLog(this.getClass(), String.format("thread = %s,key = %s,lock success", Thread.currentThread().getName(), lockEntity.getKey()));
        } else {
            //retryLock
            lockService.retryLock(executorService(), lockTimerEntityMap(), lockManager, lockEntity);
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
