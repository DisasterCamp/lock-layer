package com.disaster.locklayer.locklayer.infrastructure.utils;

import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Mac util.
 *
 * @author disaster
 * @version 1.0
 */
public class MacUtil {
    private static Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * Get current ip local mac string.
     *
     * @return the string
     */
    @SneakyThrows
    public static String getCurrentIpLocalMac() {
        InetAddress ia = null;
        ia = InetAddress.getLocalHost();
        if (cache.containsKey(ia.getCanonicalHostName())) return cache.get(ia.getCanonicalHostName());
        byte[] mac = new byte[0];
        // NetworkInterface.getByInetAddress(ia) Obtain the NIC information based on the ip address
        mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        StringBuffer sb = new StringBuffer("");
        appendMac(sb, mac);
        cache.put(ia.getCanonicalHostName(), sb.toString());
        return sb.toString();
    }


    /**
     * Get all local mac list.
     *
     * @return the list
     */
    @SneakyThrows
    public static List<String> getAllLocalMac() {
        Set<String> macs = new HashSet<>();
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        while (enumeration.hasMoreElements()) {
            StringBuffer stringBuffer = new StringBuffer();
            NetworkInterface networkInterface = enumeration.nextElement();
            if (networkInterface != null) {
                byte[] bytes = networkInterface.getHardwareAddress();
                if (bytes != null) {
                    appendMac(stringBuffer, bytes);
                    String mac = stringBuffer.toString();
                    macs.add(mac);
                }
            }
        }
        List<String> macList = new ArrayList<>(macs);
        return macList;
    }

    private static void appendMac(StringBuffer stringBuffer, byte[] bytes) {
        if (Objects.isNull(bytes)) return;
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                stringBuffer.append("-");
            }
            int tmp = bytes[i] & 0xff;
            String str = Integer.toHexString(tmp);
            if (str.length() == 1) {
                stringBuffer.append("0" + str);
            } else {
                stringBuffer.append(str);
            }
        }
    }
}
