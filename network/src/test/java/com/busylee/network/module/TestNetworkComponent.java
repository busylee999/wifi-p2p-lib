package com.busylee.network.module;

import com.busylee.network.NetworkEngineTest;
import com.busylee.network.NetworkManagerTest;
import com.busylee.network.SessionManagerTest;
import com.busylee.network.session.SessionFactory;

import javax.inject.Singleton;

import dagger.Component;
/**
 * Created by busylee on 01.09.16.
 */
@Singleton
@Component(modules = {
        MainModule.class,
        UdpModuleMock.class,
        SessionModule.class,
        SessionModuleMock.class,
        MainModuleMock.class,
        UtilsModule.class
}
)
public interface TestNetworkComponent {
    void inject(NetworkEngineTest networkEngineTest);
    void inject(NetworkManagerTest networkManagerTest);
    void inject(SessionManagerTest sessionManagerTest);

    SessionFactory getSessionFactory();
}
