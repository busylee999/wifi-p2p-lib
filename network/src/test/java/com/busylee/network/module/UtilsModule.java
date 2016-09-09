package com.busylee.network.module;

import android.os.HandlerThread;

import com.busylee.network.testutils.TLoop;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 09.09.16.
 */
@Module
public class UtilsModule {

    @Provides @Singleton
    public TLoop providesTLoop(
            @Named("ping") HandlerThread pingThread,
            @Named("sending") HandlerThread sendingThread,
            @Named("receiving") HandlerThread receivingThread

    ){
        return new TLoop()
                .add(pingThread)
                .add(sendingThread)
                .add(receivingThread);
    }
}
