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
        if (loggerCache.containsKey(aClass.getName())) {
            Logger log = loggerCache.get(aClass.getName());
            if (log.isDebugEnabled()) {
                log.info(msg);
            } else {
                System.out.println(msg);
            }
        }else {
            Logger log = LoggerFactory.getLogger(aClass);
            loggerCache.put(aClass.getName(), log);
            if (log.isDebugEnabled()) {
                log.info(msg);
            } else {
                System.out.println(msg);
            }
        }
    }
}
