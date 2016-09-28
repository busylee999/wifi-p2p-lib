package com.busylee.network;

/**
 * Created by busylee on 28.09.16.
 */

public interface Logger {
    void d(String tag, String message);
    void w(String tag, String message);
    void e(String tag, String message);
    void e(String tag, String message, Throwable e);
}
