package com.busylee.network;

import com.busylee.network.message.Message;

import java.net.InetAddress;

/**
 * Created by busylee on 02.08.16.
 */
public class Utils {
    @Deprecated
    public static String convertIpToFullMs(String ip, String message) {
        return "{\"ip\":\"" + ip + "\", \"message\":\"" + message+ "\"}";
    }

}
