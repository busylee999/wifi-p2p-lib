package com.busylee.network;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by busylee on 30.07.16.
 */
public interface Network {
    void sendMessageBroadcast(String message);
    void sendPrivateMessage(String testIp, String testMessage);

    InetAddress getIpAddress();

    interface NetworkListener extends Observer {
        @Override
        void update(Observable observable, Object data);
    }
}
