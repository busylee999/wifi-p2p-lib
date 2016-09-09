package com.busylee.network.module;

import android.os.HandlerThread;

import com.busylee.network.session.SessionManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 02.09.16.
 */
@Module(includes = UdpModuleMock.class)
public class SessionModuleMock {

    @Provides @Singleton
    public SessionManager provideSessionManager(
            @Named("ping") HandlerThread pingThread) {
        return new SessionManager(pingThread);
    }

}
