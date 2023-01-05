package com.disaster.locklayer.infrastructure.utils;


import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type Yml util.
 *
 * @author disaster
 * @version 1.0
 */
public class YmlUtil {

    /**
     * key:file index
     * value:Configuration file content
     */
    private static Map<String, LinkedHashMap> ymls = new HashMap<>();

    /**
     * string:The name of the file to be queried by the current thread
     */
    private static ThreadLocal<String> nowFileName = new ThreadLocal<>();

    static {
        loadYml("application-lock-layer.yml");
    }

    /**
     * load yaml
     *
     * @param fileName the file name
     */
    public static void loadYml(String fileName) {
        nowFileName.set(fileName);
        if (!ymls.containsKey(fileName)) {
            ymls.put(fileName, new Yaml().loadAs(YmlUtil.class.getResourceAsStream("/" + fileName), LinkedHashMap.class));
        }
    }

    /**
     * Gets value.
     *
     * @param key the key
     * @return the value
     */
    public static Object getValue(String key) {
        String[] keys = key.split("[.]");

        Map ymlInfo = (Map) ymls.get(nowFileName.get()).clone();
        for (int i = 0; i < keys.length; i++) {
            Object value = ymlInfo.get(keys[i]);
            if (i < keys.length - 1) {
                ymlInfo = (Map) value;
            } else if (value == null) {
                throw new RuntimeException("key is no found");
            } else {
                return value;
            }
        }
        return null;
    }

    /**
     * Gets value.
     *
     * @param fileName the file name
     * @param key      the key
     * @return the value
     */
    public static Object getValue(String fileName, String key) {
        loadYml(fileName);
        return getValue(key);
    }


}
