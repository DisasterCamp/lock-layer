package com.disaster.locklayer.infrastructure.asset;

import org.apache.commons.lang3.StringUtils;

public class Assert {
    public static void AssertStrIsEmpty(String str){
        if (StringUtils.isBlank(str)){
            new NullPointerException("str can't empty or null");
        }
    }
}
