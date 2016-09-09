package com.busylee.network.module;

import android.os.HandlerThread;
import android.os.Process;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 02.09.16.
 */
@Module
public class HandlerThreadModule {

    private static final int THREAD_PRIORITY = Process.THREAD_PRIORITY_BACKGROUND;

    @Provides
    @Named("sending") @Singleton
    public HandlerThread provideSensingThread() {
        return new HandlerThread("SendingThread", THREAD_PRIORITY);
    }

    @Provides @Named("receiving") @Singleton
    public HandlerThread provideReceivingThread() {
        return new HandlerThread("SendingThread", THREAD_PRIORITY);
    }

    @Provides @Named("ping") @Singleton
    public HandlerThread providePingThread() {
        return new HandlerThread("SessionPingThread", THREAD_PRIORITY);
    }
}
