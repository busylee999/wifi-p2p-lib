package com.busylee.network.module;

/**
 * Created by busylee on 01.09.16.
 */
public class TestBuilder {

    public TestNetworkComponent build() {
        return DaggerTestNetworkComponent.builder()
                .mainModule(new MainModule())
                .gsonModule(new GsonModule())
                .udpModuleMock(new UdpModuleMock())
                .sessionModule(new SessionModule())
                .sessionModuleMock(new SessionModuleMock())
                .utilsModule(new UtilsModule())
                .build();
    }
}
