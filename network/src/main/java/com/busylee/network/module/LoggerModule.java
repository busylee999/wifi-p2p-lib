package com.busylee.network.module;

import com.busylee.network.Logger;
import com.busylee.network.NetworkEngine;
import com.busylee.network.NetworkManager;
import com.busylee.network.session.SessionFactory;
import com.busylee.network.session.SessionManager;
import com.busylee.network.utils.AndroidLogger;
import com.busylee.network.utils.LoggerChain;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 02.09.16.
 */
@Module
public class LoggerModule {

    ArrayList<Logger> loggers = new ArrayList<>();

    public LoggerModule(Logger... loggers) {
        for(Logger logger: loggers) {
            this.loggers.add(logger);
        }
    }

    @Provides @Singleton
    public Logger providesLogger() {
        LoggerChain loggerChain = LoggerChain.empty().around(
                new AndroidLogger()
        );
        for(Logger logger: loggers) {
            loggerChain.around(logger);
        }
        return loggerChain;
    }

}
