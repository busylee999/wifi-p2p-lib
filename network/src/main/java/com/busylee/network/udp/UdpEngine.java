package com.busylee.network.udp;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by busylee on 30.08.16.
 */
public interface UdpEngine {
    String waitForNextMessage() throws SocketException, SocketTimeoutException;

    boolean sendMessage(String message) throws SocketException;

    InetAddress getIpAddress();
}
