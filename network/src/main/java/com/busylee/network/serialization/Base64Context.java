package com.busylee.network.serialization;

import android.util.Base64;

import com.busylee.network.Network;
import com.busylee.network.message.Message;
import com.google.gson.Gson;

import java.util.Observable;

import javax.inject.Inject;

/**
 * Created by busylee on 01.09.16.
 */
public class Base64Context implements SerializationContext {

    private final Gson gson;

    @Inject
    public Base64Context(Gson gson) {
        this.gson = gson;
    }

    @Override
    public byte[] serialize(Message message) {
        String messageData = message.getData();
        if(messageData != null) {
            message = Message.Builder.from(message)
                    .setData(Base64.encodeToString(messageData.getBytes(), Base64.DEFAULT))
                    .build();
        }
        return message.toString().getBytes();
    }

    @Override
    public Message deserialize(byte[] bytes) {
        Message message = gson.fromJson(new String(bytes), Message.class);
        String messageData = message.getData();
        if(messageData != null) {
            byte[] decode = Base64.decode(messageData.getBytes(), Base64.DEFAULT);
            message = Message.Builder.from(message)
                    .setData(new String(decode))
                    .build();
        }

        return message;
    }
}
