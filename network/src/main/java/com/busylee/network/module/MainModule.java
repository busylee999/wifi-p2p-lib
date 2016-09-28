package com.busylee.network.module;

import com.busylee.network.Logger;
import com.busylee.network.NetworkEngine;
import com.busylee.network.NetworkManager;
import com.busylee.network.session.SessionFactory;
import com.busylee.network.session.SessionManager;
import com.busylee.network.utils.AndroidLogger;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 02.09.16.
 */
@Module
public class MainModule {

    @Provides
    public Logger providesLogger() {
        return new AndroidLogger();
    }

}
