package com.busylee.network;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.testutils.TUtils;
import com.busylee.network.udp.UdpEngine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 04.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class PeerNetworkEngineTest {
    PeerNetworkEngine networkEngine;
    HandlerThread sendingThread;
    HandlerThread receivingThread;
    UdpEngine udpEngineMock;
    Network.NetworkListener networkListenerMock;

    @Before
    public void setup() {
        udpEngineMock = mock(UdpEngine.class);
        networkListenerMock = mock(Network.NetworkListener.class);
        sendingThread
                = new HandlerThread("SendingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        receivingThread
                = new HandlerThread("ReceivingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        networkEngine = new PeerNetworkEngine(0, udpEngineMock, sendingThread, receivingThread);
        networkEngine.addObserver(networkListenerMock);
        networkEngine.start();
    }

    @Test
    public void shouldSendPeerInfo() throws SocketException, UnknownHostException {
        final InetAddress fakeAddress = InetAddress.getByName("1.1.1.1");
        final String expectedMessage =
                "{\"addressFrom\":\"1.1.1.1\",\"command\":\"PEER\"}";
        when(udpEngineMock.getIpAddress()).thenReturn(fakeAddress);
        TUtils.oneTask(sendingThread);
        verify(udpEngineMock, times(1)).sendMessage(expectedMessage);
    }

}
