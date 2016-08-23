package com.busylee.network;

import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by busylee on 23.08.16.
 */

public class TConsts {
    public static final long GROUP_PEER_ID = new Random().nextLong();
    public static final Message GROUP_PEER_MESSAGE;
    public static final Message GROUP_DATA_MESSAGE;
    public static final String GROUP_MESSAGE_TEXT = "test";
    public static final GroupEndpoint GROUP_ENDPOINT
            = new GroupEndpoint(String.valueOf(GROUP_PEER_ID));

    public static String ENDPOINT_ID;
    public static InetAddress ADDRESS;
    public static final Message SESSION_INVITE_MESSAGE;
    public static final Endpoint INVITE_ENDPOINT;

    static {
        try {
            ADDRESS = InetAddress.getByName("1.1.1.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        GROUP_PEER_MESSAGE = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setId(String.valueOf(GROUP_PEER_ID))
                .build();
        GROUP_DATA_MESSAGE = new Message.Builder()
                .setCommand(Message.Command.DATA)
                .setId(String.valueOf(GROUP_PEER_ID))
                .setData(GROUP_MESSAGE_TEXT)
                .build();


        INVITE_ENDPOINT = new UserEndpoint(ENDPOINT_ID, ADDRESS);

        SESSION_INVITE_MESSAGE = new Message.Builder()
                .setCommand(Message.Command.INVITE)
                .setAddressFrom(ADDRESS)
                .build();
    }
}
