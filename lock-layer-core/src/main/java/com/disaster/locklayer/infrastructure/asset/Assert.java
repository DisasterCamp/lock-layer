package com.disaster.locklayer.infrastructure.asset;

import org.apache.commons.lang3.StringUtils;

/**
 * The type Assert.
 *
 * @author disaster
 * @version 1.0
 */
public class Assert {
    /**
     * Assert str is empty.
     *
     * @param str the str
     */
    public static void AssertStrIsEmpty(String str){
        if (StringUtils.isBlank(str)){
            new NullPointerException("str can't empty or null");
        }
    }
}
