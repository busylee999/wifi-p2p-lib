package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Observable;
import java.util.Observer;


/**
 * Created by busylee on 04.08.16.
 */
public class UdpEndpointSession extends EndpointSession implements Observer {

    private static final String TAG = "UdpEndpointSession";
    private final NetworkEngine networkEngine;
    private final UserEndpoint endpoint;
    private SessionListener sessionListener;
    private final Gson gson;
    public UdpEndpointSession(UserEndpoint endpoint, NetworkEngine networkEngine) {
        this.endpoint = endpoint;
        this.networkEngine = networkEngine;
        this.networkEngine.addObserver(this);
        this.gson = new GsonBuilder().create();
    }

    @Override
    public void ping() {

    }

    @Override
    public EState getState() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void sendMessage(String messageString) {
        Message message = new Message.Builder()
                .setCommand(Message.Command.DATA)
                .setEndpoint(endpoint)
                .setData(messageString)
                .build();
        sendMessage(message);
    }

    @Override
    public void sendMessage(Message message) {
        networkEngine.sendMessageBroadcast(message.toString());
    }

    @Override
    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
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

        switch (message.getCommand()) {
            case Message.Command.DATA:
                if(sessionListener != null) {
                    sessionListener.onNewMessage(message.getData());
                }
                break;
        }
    }

    @Override
    public UserEndpoint getEndpoint() {
        return endpoint;
    }
}
