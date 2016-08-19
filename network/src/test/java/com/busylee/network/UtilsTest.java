package com.busylee.network;

import org.junit.Test;

/**
 * Created by busylee on 02.08.16.
 */
public class UtilsTest {
    final String ip = "1";
    final String message = "2";
    final String packet = "{\"ip\":\"" + ip + "\", \"message\":\"" + message+ "\"}";
    @Test
    public void shouldConvertIpAndMessageIntoPacket() {
        Assert.assertEquals(Utils.convertIpToFullMs(ip, message), packet);
    }
}
