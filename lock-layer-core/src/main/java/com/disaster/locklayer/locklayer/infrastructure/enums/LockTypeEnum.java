package com.disaster.locklayer.locklayer.infrastructure.enums;

/**
 * The enum Lock type enum.
 *
 * @author disaster
 * @version 1.0
 */
public enum LockTypeEnum {
    /**
     * Redisson lock type enum.
     */
    REDISSON("redisson"),
    /**
     * Jedis lock type enum.
     */
    JEDIS("jedis");
    private String value;

    LockTypeEnum(String value) {
        this.value = value;
    }
}
