package com.busylee.network.testutils;

import android.os.HandlerThread;

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
        return toBytes(message.toString());
    }

    public static byte[] toBytes(String message) {
        return message.getBytes();
    }

}
