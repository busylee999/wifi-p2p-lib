package com.busylee.network.udp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import android.util.Log;

import com.busylee.network.Logger;
import com.busylee.network.utils.AndroidLogger;

import javax.inject.Inject;

/**
 * Created by busylee on 14.07.15.
 */
public class UdpEngineImpl implements UdpEngine {

    private static final String TAG = "UdpEngine";
    private static final String REMOTE_KEY = "b0xeeRem0tE!";
    private static final int DISCOVERY_PORT = 2562;
    private static final int TIMEOUT_MS = 0;

    // TODO: Vary the challenge, or it's not much of a challenge :)
    private static final String mChallenge = "myvoice";
    private final Logger logger;

    private DatagramSocket mSocket;

    public UdpEngineImpl() {
        this(new AndroidLogger());
    }

    @Inject
    public UdpEngineImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public byte[] waitForNextMessage() throws SocketException, SocketTimeoutException {
        try {
            DatagramSocket socket = getSocket();
            return tryListenForString(socket);
        } catch (SocketException e) {
            logger.e(TAG, "Socket creation error", e);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            logger.e(TAG, "Input/Output exception occured", e);
        }
        return null;
    }

    @Override
    public boolean sendMessage(byte[] message) throws SocketException {
        logger.d(TAG, "Sending message " + message);
        try {
            DatagramSocket socket = getSocket();

            InetAddress broadcastAddress = getBroadcast(getIpAddress());

            if(broadcastAddress == null)
                throw new SocketException("broadcast address is null");

            DatagramPacket packet = new DatagramPacket(message, message.length,
                    broadcastAddress, DISCOVERY_PORT);
            socket.send(packet);
            return true;
        } catch (SocketException e) {
            logger.e(TAG, "Socket creation error", e);
        }  catch (IOException e) {
            logger.e(TAG, "Input/Output exception occured", e);
        }
        return false;
    }

    private DatagramSocket createBoundSocket() throws SocketException {
        DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
        socket.setBroadcast(true);
        socket.setSoTimeout(TIMEOUT_MS);
        return socket;
    }

    private synchronized DatagramSocket getSocket() throws SocketException {
        if(mSocket == null) {
            mSocket = createBoundSocket();
        }

        return mSocket;
    }

    private byte[] tryListenForString(DatagramSocket socket) throws IOException {
        //TODO: we reach bound in 1024 with messages we have
        byte[] buf = new byte[10024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
    }

    @Override
    public InetAddress getIpAddress() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration < NetworkInterface > networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements();) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration< InetAddress > IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements();) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            logger.e(TAG, ex.toString());
        }
        return myAddr;
    }

    private InetAddress getBroadcast(InetAddress inetAddr) {

        if (inetAddr != null) {
            NetworkInterface temp;
            InetAddress iAddr = null;
            try {
                temp = NetworkInterface.getByInetAddress(inetAddr);
                List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

                for (InterfaceAddress inetAddress : addresses)
                    iAddr = inetAddress.getBroadcast();

                logger.d(TAG, "iAddr=" + iAddr);
                return iAddr;

            } catch (SocketException e) {

                e.printStackTrace();
                logger.d(TAG, "getBroadcast" + e.getMessage());
            }
        }

        return null;
    }

}
