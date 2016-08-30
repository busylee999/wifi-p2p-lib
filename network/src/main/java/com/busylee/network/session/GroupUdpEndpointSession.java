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
    
    private final GroupEndpoint groupEndpoint;
    private final Gson gson;

    GroupUdpEndpointSession(GroupEndpoint groupEndpoint, NetworkEngine networkEngine) {
        super(groupEndpoint, networkEngine);
        this.groupEndpoint = groupEndpoint;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void update(Observable observable, Object data) {
        Message message = gson.fromJson((String) data, Message.class);
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

        switch (message.getCommand()) {
            case Message.Command.DATA:
                if(sessionListener != null) {
                    sessionListener.onNewMessage(groupEndpoint, message.getData());
                }
                break;
        }
    }

    @Override
    public void ping() {
        //TODO
    }

    @Override
    public void sendMessage(String stringMessage) {
        Message message = new Message.Builder()
                .setId(groupEndpoint.getId())
                .setCommand(Message.Command.DATA)
                .setData(stringMessage)
                .build();
        networkEngine.sendMessageBroadcast(message.toString());
    }

    @Override
    public GroupEndpoint getEndpoint() {
        return groupEndpoint;
    }
}
