package com.disaster.locklayer.infrastructure.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Logger util.
 *
 * @author disaster
 * @version 1.0
 */
public class LoggerUtil {
    private static Map<String, Logger> loggerCache = new ConcurrentHashMap();

    /**
     * Println log.
     *
     * @param aClass the a class
     * @param msg    the msg
     */
    public static void printlnLog(Class aClass, String msg) {
        if (LockConfigUtil.getLogEnable()){
            Logger log = loggerCache.getOrDefault(aClass.getName(), LoggerFactory.getLogger(aClass));
            if (log.isInfoEnabled()||log.isDebugEnabled()||log.isErrorEnabled()||log.isWarnEnabled()) {
                log.info(msg);
            } else {
                System.out.println(msg);
            }
        }
    }
}
