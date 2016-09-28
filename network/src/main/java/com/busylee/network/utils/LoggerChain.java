package com.busylee.network.utils;

import com.busylee.network.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by busylee on 28.09.16.
 */

public class LoggerChain implements Logger {

    public List<Logger> loggerList = new ArrayList<>();

    @Override
    public void d(String tag, String message) {
        for (Logger logger : loggerList) {
            logger.d(tag, message);
        }
    }

    @Override
    public void w(String tag, String message) {
        for (Logger logger : loggerList) {
            logger.w(tag, message);
        }
    }

    @Override
    public void e(String tag, String message) {
        for (Logger logger : loggerList) {
            logger.e(tag, message);
        }
    }

    @Override
    public void e(String tag, String message, Throwable e) {
        for (Logger logger : loggerList) {
            logger.e(tag, message, e);
        }
    }

    public LoggerChain around(Logger logger) {
        loggerList.add(logger);
        return this;
    }

    public static LoggerChain empty() {
        return new LoggerChain();
    }
}
