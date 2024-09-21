package com.disaster.locklayer.locklayer.infrastructure.utils;

import com.disaster.locklayer.infrastructure.enums.LogLevel;
import com.disaster.locklayer.infrastructure.utils.LockConfigUtil;
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
    public static void println(Class aClass, String msg) {
        if (LockConfigUtil.getLogEnable()) {
            Logger log = loggerCache.getOrDefault(aClass.getName(), LoggerFactory.getLogger(aClass));
            if (log.isInfoEnabled() || log.isDebugEnabled() || log.isErrorEnabled() || log.isWarnEnabled()) {
                log.info(msg);
            } else {
                System.out.println(msg);
            }
        }
    }

    /**
     * Println log.
     *
     * @param aClass   the a class
     * @param msg      the msg
     * @param logLevel the log level
     */
    public static void println(Class aClass, String msg, LogLevel logLevel) {
        if (LockConfigUtil.getLogEnable()) {
            Logger log = loggerCache.getOrDefault(aClass.getName(), LoggerFactory.getLogger(aClass));
            if (log.isInfoEnabled() || log.isDebugEnabled() || log.isErrorEnabled() || log.isWarnEnabled()) {
                switch (logLevel) {
                    case INFO:
                        log.info(msg);
                        break;
                    case DEBUG:
                        log.debug(msg);
                        break;
                    case ERROR:
                        log.error(msg);
                        break;
                    case WARN:
                        log.warn(msg);
                        break;
                    default:
                        log.info(msg);
                }
            } else {
                System.out.println(msg);
            }
        }
    }
}
