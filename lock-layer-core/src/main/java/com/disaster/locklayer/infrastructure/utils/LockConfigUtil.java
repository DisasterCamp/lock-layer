package com.disaster.locklayer.infrastructure.utils;

import com.disaster.locklayer.infrastructure.constant.Constants;

import java.util.Objects;

/**
 * The type Lock config util.
 */
public class LockConfigUtil {
    private static Integer MAX_EXPIRE_COUNT;
    private static Integer MAX_RETRY_TIME;
    private static Long MAX_EXPIRE_TIME;
    private static Boolean LOG_ENABLE;
    private static Integer MAX_REENTRY_COUNT;
    private static String PREFIX = "lock.layer.";

    static {
        Integer maxExpireCount = (Integer) YmlUtil.getValue(PREFIX + "max_expire_count");
        Integer maxRetryTime = (Integer) YmlUtil.getValue(PREFIX + "max_retry_time");
        Integer maxExpireTime = (Integer) YmlUtil.getValue(PREFIX + "max_expire_time");
        Integer maxReentryCount = (Integer) YmlUtil.getValue(PREFIX + "max_reentry_count");
        Boolean logEnable = (Boolean) YmlUtil.getValue(PREFIX + "log.enable");
        MAX_EXPIRE_COUNT = Objects.isNull(maxExpireCount) ? Constants.MAX_EXPIRE_COUNT : maxExpireCount;
        MAX_RETRY_TIME = Objects.isNull(maxRetryTime) ? Constants.MAX_RETRY_TIME : maxRetryTime;
        MAX_EXPIRE_TIME = Objects.isNull(maxExpireTime) ? Constants.MAX_EXPIRE_TIME : maxExpireTime;
        LOG_ENABLE = Objects.isNull(logEnable) ? false : logEnable;
        MAX_REENTRY_COUNT = Objects.isNull(maxReentryCount) ? 3 : maxReentryCount;
    }

    /**
     * Gets max expire count.
     *
     * @return the max expire count
     */
    public static Integer getMaxExpireCount() {
        return MAX_EXPIRE_COUNT;
    }

    /**
     * Gets max retry time.
     *
     * @return the max retry time
     */
    public static Integer getMaxRetryTime() {
        return MAX_RETRY_TIME;
    }

    /**
     * Gets prefix.
     *
     * @return the prefix
     */
    public static String getPREFIX() {
        return PREFIX;
    }

    /**
     * Gets max expire time.
     *
     * @return the max expire time
     */
    public static Long getMaxExpireTime() {
        return MAX_EXPIRE_TIME;
    }

    /**
     * Gets max reentry count.
     *
     * @return the max reentry count
     */
    public static Integer getMaxReentryCount() {
        return MAX_REENTRY_COUNT;
    }

    /**
     * Sets max reentry count.
     *
     * @param maxReentryCount the max reentry count
     */
    public static void setMaxReentryCount(Integer maxReentryCount) {
        MAX_REENTRY_COUNT = maxReentryCount;
    }

    /**
     * Gets log enable.
     *
     * @return the log enable
     */
    public static Boolean getLogEnable() {
        return LOG_ENABLE;
    }

}
