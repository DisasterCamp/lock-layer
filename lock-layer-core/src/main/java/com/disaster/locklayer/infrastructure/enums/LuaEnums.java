package com.disaster.locklayer.infrastructure.enums;

public enum LuaEnums {
    LOCK("if redis.call('setnx',KEYS[1],ARGV[1]) == 1\n" +
            "then\n" +
            "redis.call('expire',KEYS[1],ARGV[2])\n" +
            "return 1\n" +
            "else return 0\n" +
            "end"),
    LOCK_MILLS("if redis.call('setnx',KEYS[1],ARGV[1]) == 1\n" +
            "then\n" +
            "redis.call('pexpire',KEYS[1],ARGV[2])\n" +
            "return 1\n" +
            "else return 0\n" +
            "end"),
    UN_LOCK("if(redis.call('get',KEYS[1])==ARGV[1])\n" +
            "then return\n" +
            "redis.call('del',KEYS[1])\n" +
            "else return 0\n" +
            "end");
    private String luaStr;

    LuaEnums(String luaStr) {
        this.luaStr = luaStr;
    }

    LuaEnums() {
    }

    public String getLuaStr() {
        return luaStr;
    }

    public void setLuaStr(String luaStr) {
        this.luaStr = luaStr;
    }
}
