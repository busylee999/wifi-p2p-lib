package com.busylee.network.module;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.Logger;
import com.busylee.network.Network;
import com.busylee.network.NetworkEngine;
import com.busylee.network.udp.UdpEngine;
import com.busylee.network.udp.UdpEngineImpl;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by busylee on 02.09.16.
 */
@Module(includes = HandlerThreadModule.class)
public class UdpModule {

    @Provides
    public UdpEngine provideUdpEngine() {
        return new UdpEngineImpl();
    }

    @Provides
    public NetworkEngine provideNetwork(UdpEngine udpEngine,
                                        @Named("sending") HandlerThread sendingThread,
                                        @Named("receiving") HandlerThread receivingThread, Logger logger) {
        return new NetworkEngine(udpEngine, sendingThread, receivingThread, logger);
    }

}
