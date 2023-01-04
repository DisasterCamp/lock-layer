package com.disaster.locklayer.infrastructure.utils;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

/**
 * The type Resource reader util.
 *
 * @author disaster
 * @version 1.0
 */
public class ResourceReaderUtil {

    /**
     * Read resource map.
     *
     * @param resource the resource
     * @return the map
     */
    @SneakyThrows
    public static Map<String,String> readResource(String resource) {
        InputStream inputStream = ResourceReaderUtil.class.getClassLoader().getResourceAsStream(resource);
        Map<String, String> paramMap = Maps.newHashMap();
        if (Objects.nonNull(inputStream)){
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String paramStr = "";
            String key = "";
            while ((paramStr = br.readLine()) != null) {
                String[] splitArray = paramStr.split("=");
                key = splitArray[0].substring(splitArray[0].lastIndexOf(".") + 1);
                paramMap.put(key, splitArray[1]);
            }
            return paramMap;
        }
        return paramMap;
    }

}
