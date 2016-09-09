package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.serialization.SerializationContext;
import com.busylee.network.serialization.SerializationListener;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by busylee on 23.08.16.
 */

public class UserUdpEndpointSession extends UdpEndpointSession implements SerializationListener {

    private static final String TAG = "UdpEndpointSession";

    private final UserEndpoint endpoint;
    private final long expiredBound;
    private final Gson gson;
    private long lastActionTime;

    UserUdpEndpointSession(UserEndpoint endpoint,
                           NetworkEngine networkEngine,
                           SerializationContext serializationContext) {
        this(endpoint, networkEngine, serializationContext, DEFAULT_EXPIRED_BOUND);
    }

    UserUdpEndpointSession(UserEndpoint endpoint,
                           NetworkEngine networkEngine,
                           SerializationContext serializationContext,
                           int expiredBound) {
        super(endpoint, networkEngine, serializationContext);
        this.endpoint = endpoint;
        this.networkEngine.addObserver(serializationContext);
        this.expiredBound = expiredBound;
        this.gson = new GsonBuilder().create();
        updateLastActionTime();
    }

    @Override
    public void ping() {
        //TODO need to be implement
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
    public void sendDataMessage(String messageString) {
        Message message = new Message.Builder()
                .setCommand(Message.Command.DATA)
                .setEndpoint(getEndpoint())
                .setData(messageString)
                .build();
        sendMessage(message);
    }

    @Override
    public void onMessage(Message message) {
        //TODO should be safe for null
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
}
