package com.busylee.network.testutils;

import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by busylee on 09.09.16.
 */

public class TLoop {

    private List<HandlerThread> handlerThreads = new ArrayList<>();

    public TLoop add(HandlerThread handlerThread) {
        if(!handlerThreads.contains(handlerThread)) {
            handlerThreads.add(handlerThread);
        }

        return this;
    }

    public TLoop loop(int times) {
        for (int i = 0; i < times; i ++) {
            for(HandlerThread handlerThread: handlerThreads) {
                TUtils.oneTask(handlerThread);
            }
        }

        return this;
    }
}
