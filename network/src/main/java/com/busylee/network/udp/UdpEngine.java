package com.busylee.network.udp;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by busylee on 30.08.16.
 */
public interface UdpEngine {
    byte[] waitForNextMessage() throws SocketException, SocketTimeoutException;

    boolean sendMessage(byte[] message) throws SocketException;

    InetAddress getIpAddress();
}
