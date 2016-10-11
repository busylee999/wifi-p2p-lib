package com.busylee.network.module;

import com.busylee.network.NetworkEngine;
import com.busylee.network.NetworkManager;
import com.busylee.network.session.SessionFactory;
import com.busylee.network.udp.UdpEngine;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by busylee on 10.10.16.
 */
@Singleton
@Component(
        modules = {
        GsonModule.class,
        HandlerThreadModule.class,
        LoggerModule.class,
        SessionModule.class,
        UdpModule.class
})
public interface NetworkComponent {
  // explicit for derivatives
  NetworkManager networkManager();
}
