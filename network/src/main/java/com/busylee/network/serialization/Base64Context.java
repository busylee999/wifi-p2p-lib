package com.busylee.network.serialization;

import android.util.Base64;

import com.busylee.network.Network;
import com.busylee.network.message.Message;
import com.google.gson.Gson;

import java.util.Observable;

/**
 * Created by busylee on 01.09.16.
 */
public class Base64Context implements SerializationContext {

    private final Gson gson;
    private SerializationListener serializationListener;

    public Base64Context(Gson gson) {
        this.gson = gson;
    }
    @Override
    public void sendMessage(Network network, Message message) {
        String messageData = message.getData();
        if(messageData != null) {
            message = Message.Builder.from(message)
                    .setData(Base64.encodeToString(messageData.getBytes(), Base64.DEFAULT))
                    .build();
        }
        network.sendMessageBroadcast(message.toString().getBytes());
    }

    @Override
    public void setListener(SerializationListener serializationListener) {
        this.serializationListener = serializationListener;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(serializationListener == null) {
            return;
        }
        Message message = gson.fromJson(new String((byte[]) data), Message.class);
        String messageData = message.getData();
        if(messageData != null) {
            byte[] decode = Base64.decode(messageData.getBytes(), Base64.DEFAULT);
            message = Message.Builder.from(message)
                    .setData(new String(decode))
                    .build();
        }
        serializationListener.onMessage(message);
    }

}
