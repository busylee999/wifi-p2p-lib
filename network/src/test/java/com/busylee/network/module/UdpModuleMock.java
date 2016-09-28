package com.busylee.network.module;

import android.os.HandlerThread;

import com.busylee.network.Logger;
import com.busylee.network.NetworkEngine;
import com.busylee.network.udp.UdpEngine;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by busylee on 02.09.16.
 */
@Module(includes = HandlerThreadModule.class)
public class UdpModuleMock {

    @Provides @Singleton
    public UdpEngine provideUdpEngineMock() {
        return mock(UdpEngine.class);
    }

    @Provides
    public NetworkEngine provideNetwork(UdpEngine udpEngine,
                                        @Named("sending") HandlerThread sendingThread,
                                        @Named("receiving") HandlerThread receivingThread, Logger logger) {
        return new NetworkEngine(udpEngine, sendingThread, receivingThread, logger);
    }

    @Provides @Mocked
    public NetworkEngine provideNetworkEngine() {
        return mock(NetworkEngine.class);
    }

}
