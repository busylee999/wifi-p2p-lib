package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Observable;

/**
 * Created by busylee on 23.08.16.
 */

public class UserUdpEndpointSession extends UdpEndpointSession {

    private static final String TAG = "UdpEndpointSession";
    private static final int DEFAULT_EXPIRED_BOUND = 5 * 1000; //5 sec

    private final UserEndpoint endpoint;
    private final long expiredBound;
    private final Gson gson;

    private long lastActionTime;

    UserUdpEndpointSession(UserEndpoint endpoint, NetworkEngine networkEngine) {
        this(endpoint, networkEngine, DEFAULT_EXPIRED_BOUND);
    }

    UserUdpEndpointSession(UserEndpoint endpoint, NetworkEngine networkEngine, long expiredBound) {
        super(endpoint, networkEngine);
        this.endpoint = endpoint;
        this.expiredBound = expiredBound;
        this.gson = new GsonBuilder().create();
        updateLastActionTime();
    }

    @Override
    public void update(Observable observable, Object data) {
        Message message = gson.fromJson((String) data, Message.class);
        if(message.getCommand() == null) {
            Log.w(TAG, "missing command in message = " + message);
            return;
        }

        if(endpoint.getId() != null
                && message.getId() != null
                && !endpoint.getId().equals(message.getId())) {
            return;
        }

        if(endpoint.getAddress() != null
                && message.getAddressFrom() != null
                && !endpoint.getAddress().equals(message.getAddressFrom())) {
            return;
        }

        updateLastActionTime();

        switch (message.getCommand()) {
            case Message.Command.DATA:
                if(sessionListener != null) {
                    sessionListener.onNewMessage(endpoint, message.getData());
                }
                break;
        }
    }

    @Override
    public void ping() {

    }

    @Override
    public UserEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean isExpired() {
        return lastActionTime  + expiredBound <= System.currentTimeMillis() ;
    }

    private void updateLastActionTime() {
        lastActionTime = System.currentTimeMillis();
    }

    @Override
    public void sendMessage(String messageString) {
        Message message = new Message.Builder()
                .setCommand(Message.Command.DATA)
                .setEndpoint(getEndpoint())
                .setData(messageString)
                .build();
        sendMessage(message);
    }
}
