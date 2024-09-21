package com.disaster.locklayer.locklayer.infrastructure.utils;

import com.disaster.locklayer.infrastructure.enums.LuaEnums;

/**
 * The type Lua utils.
 *
 * @author disaster
 * @version 1.0
 */
public class LuaUtils {

    /**
     * Gets lock lua str.
     *
     * @return the lock lua str
     */
    public static String getLockLuaStr() {
        return LuaEnums.LOCK.getLuaStr();
    }

    /**
     * Get lock lua mills str string.
     *
     * @return the string
     */
    public static String getLockLuaMillsStr(){
        return LuaEnums.UN_LOCK.getLuaStr();
    }

    /**
     * Get un lock lua str string.
     *
     * @return the string
     */
    public static String getUnLockLuaStr(){
        return LuaEnums.UN_LOCK.getLuaStr();
    }

}
