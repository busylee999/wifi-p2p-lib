package com.busylee.network.testutils;

import android.os.HandlerThread;
import android.util.Base64;

import com.busylee.network.message.Message;

import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowLooper;

/**
 * Created by busylee on 03.08.16.
 */
public class TUtils {
    public static void oneTask(HandlerThread handlerThread) {
        ShadowLooper looper = Shadows.shadowOf(handlerThread.getLooper());
        looper.runOneTask();
    }

    public static byte[] toBytes(Message message) {
        String data = message.getData();
        if(data != null) {
            Message message1 = Message.Builder.from(message)
                    .setData(Base64.encodeToString(data.getBytes(), Base64.DEFAULT)).build();
            return toBytes(message1.toString());
        }

        return toBytes(message.toString());
    }

    public static byte[] toBytes(String message) {
        return message.getBytes();
    }

}
