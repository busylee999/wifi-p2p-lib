package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Observable;

/**
 * Created by busylee on 23.08.16.
 */
public class GroupUdpEndpointSession extends UdpEndpointSession {

    private static final String TAG = "GroupUdpEndpointSession";

    private static final long DEFAULT_EXPIRED_BOUND = 5 * 1000; // exevy 5 sec

    private final GroupEndpoint groupEndpoint;
    private final long expiredBound;
    private final Gson gson;
    private long lastActionTime;

    GroupUdpEndpointSession(GroupEndpoint groupEndpoint, NetworkEngine networkEngine) {
        this(groupEndpoint, networkEngine, DEFAULT_EXPIRED_BOUND);
    }

    GroupUdpEndpointSession(GroupEndpoint groupEndpoint, NetworkEngine networkEngine, long expiredBound) {
        super(groupEndpoint, networkEngine);
        this.groupEndpoint = groupEndpoint;
        this.expiredBound = expiredBound;
        this.gson = new GsonBuilder().create();
        updateLastActionTime();
    }

    @Override
    public void update(Observable observable, Object data) {
        Message message = gson.fromJson(new String((byte[]) data), Message.class);
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

    private void updateLastActionTime() {
        lastActionTime = System.currentTimeMillis();
    }

    @Override
    public void ping() {
        //TODO
    }

    @Override
    public void sendDataMessage(String stringMessage) {
        Message message = new Message.Builder()
                .setId(groupEndpoint.getId())
                .setCommand(Message.Command.DATA)
                .setData(stringMessage)
                .build();
        networkEngine.sendMessageBroadcast(message.toString().getBytes());
    }

    @Override
    public GroupEndpoint getEndpoint() {
        return groupEndpoint;
    }

    @Override
    public boolean isExpired() {
        return lastActionTime + expiredBound <= System.currentTimeMillis() ;
    }
}
