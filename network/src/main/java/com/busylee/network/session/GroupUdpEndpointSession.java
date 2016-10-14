package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.Logger;
import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.serialization.SerializationContext;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Observable;

/**
 * Created by busylee on 23.08.16.
 */
public class GroupUdpEndpointSession extends UdpEndpointSession {

    private static final String TAG = "GroupUdpEndpointSession";

    private final GroupEndpoint groupEndpoint;
    private final long expiredBound;
    private final Gson gson;
    private final Logger logger;
    private long lastActionTime;

    GroupUdpEndpointSession(GroupEndpoint groupEndpoint,
                            NetworkEngine networkEngine,
                            SerializationContext serializationContext, Logger logger) {
        this(groupEndpoint, networkEngine, serializationContext, logger, DEFAULT_EXPIRED_BOUND);
    }

    GroupUdpEndpointSession(GroupEndpoint groupEndpoint,
                            NetworkEngine networkEngine,
                            SerializationContext serializationContext,
                            Logger logger,
                            long expiredBound) {
        super(groupEndpoint, networkEngine, serializationContext);
        this.logger = logger;
        this.groupEndpoint = groupEndpoint;
        this.expiredBound = expiredBound;
        this.gson = new GsonBuilder().create();
        updateLastActionTime();
    }

    private void updateLastActionTime() {
        lastActionTime = System.currentTimeMillis();
    }

    @Override
    public void ping() {
        Message message = new Message.Builder()
                .setId(groupEndpoint.getId())
                .setCommand(Message.Command.PING)
                .build();
        sendMessage(message);
    }

    @Override
    public void sendDataMessage(String stringMessage) {
        Message message = new Message.Builder()
                .setId(groupEndpoint.getId())
                .setCommand(Message.Command.DATA)
                .setData(stringMessage)
                .build();
        sendMessage(message);
    }

    @Override
    public void sendMessage(Message message) {
        logger.d(TAG, "sendMessage " + message.getCommand());
        super.sendMessage(message);
    }

    @Override
    public GroupEndpoint getEndpoint() {
        return groupEndpoint;
    }

    @Override
    public boolean isExpired() {
        return lastActionTime + expiredBound <= System.currentTimeMillis() ;
    }

    @Override
    protected void onMessage(Message message) {
        if(message.getCommand() == null) {
            Log.w(TAG, "missing command in message = " + message);
            return;
        }

        if(message.getId() == null ||
                ( groupEndpoint.getId() != null
                        && !groupEndpoint.getId().equals(message.getId())
                )) {
            return;
        }

        updateLastActionTime();

        switch (message.getCommand()) {
            case Message.Command.DATA:
                if(sessionListener != null) {
                    sessionListener.onNewMessage(groupEndpoint, message.getData());
                }
                break;
        }
    }
}
