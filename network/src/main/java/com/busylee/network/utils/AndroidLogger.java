package com.busylee.network.utils;

import android.util.Log;

import com.busylee.network.Logger;

/**
 * Created by busylee on 28.09.16.
 */

public class AndroidLogger implements Logger {
    @Override
    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    @Override
    public void e(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void e(String tag, String message, Throwable e) {
        Log.e(tag, message, e);
    }
}
