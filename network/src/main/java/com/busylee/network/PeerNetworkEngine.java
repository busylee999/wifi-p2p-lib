package com.busylee.network;

import android.os.HandlerThread;

import com.busylee.network.message.Message;
import com.busylee.network.udp.UdpEngine;

import java.net.InetAddress;

/**
 * Created by busylee on 04.08.16.
 */
public class PeerNetworkEngine extends NetworkEngine {
    private static final int AUTH = 1001;
    private final int authDelay;

    public PeerNetworkEngine(int authDelay, UdpEngine udpEngine) {
        super(udpEngine);
        this.authDelay = authDelay;
    }

    public PeerNetworkEngine(int authDelay, UdpEngine udpEngine, HandlerThread sendMessageThread, HandlerThread receiveThread) {
        super(udpEngine, sendMessageThread, receiveThread);
        this.authDelay = authDelay;
    }

    @Override
    public void start() {
        super.start();
        sendAuthDelay();
    }

    private void sendAuthDelay() {
        android.os.Message handlerMessage = new android.os.Message();
        handlerMessage.what = AUTH;
        postToSendingThread(handlerMessage, authDelay);
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {
        if(msg.what == AUTH) {
            InetAddress inetAddress = getIpAddress();
            if(inetAddress != null) {
                Message message = new Message.Builder()
                        .setCommand(Message.Command.PEER)
                        .setAddressFrom(inetAddress)
                        .build();
                //TODO think about it
                onSendMessage(message.toString());
            }
            sendAuthDelay();
            return false;
        }
        return super.handleMessage(msg);
    }
}
