package com.busylee.network.session;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.NetworkEngine;
import com.busylee.network.TConsts;
import com.busylee.network.message.Message;
import com.busylee.network.serialization.Base64Context;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TUtils;
import com.busylee.network.udp.UdpEngine;
import com.busylee.network.utils.AndroidLogger;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 03.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class UdpBroadcastSessionTest {

    NetworkEngine networkEngine;
    HandlerThread sendingThread;
    HandlerThread receivingThread;
    UdpEngine udpEngineMock;
    UdpBroadcastSession udpBroadcastSession;

    @Before
    public void setup() {
        udpEngineMock = mock(UdpEngine.class);
        sendingThread
                = new HandlerThread("SendingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        receivingThread
                = new HandlerThread("ReceivingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        networkEngine = new NetworkEngine(udpEngineMock, sendingThread, receivingThread, new AndroidLogger());
        networkEngine.start();
        udpBroadcastSession = new UdpBroadcastSession(networkEngine, new Base64Context(new GsonBuilder().create()));
    }

    @Test
    public void shouldCallNetworkEngineBroadcast() throws UnknownHostException, SocketException {
        final Message message = new Message.Builder()
                .setId("id")
                .setData("data")
                .setAddressTo("1.1.1.1")
                .build();
        udpBroadcastSession.sendMessage(message);
        TUtils.oneTask(sendingThread);
        verify(udpEngineMock, times(1)).sendMessage(TUtils.toBytes(message));
    }

    @Test
    public void shouldNotifyEndPointListenerAboutPeer() throws UnknownHostException, SocketException, SocketTimeoutException {
        UdpBroadcastSession.EndPointListener endPointListenerMock = mock(UdpBroadcastSession.EndPointListener.class);
        final Message message = new Message.Builder()
                .setCommand(Message.Command.PING)
                .setId("id")
                .setAddressFrom("1.1.1.1")
                .build();
        udpBroadcastSession.setEndpointListener(endPointListenerMock);
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        TUtils.oneTask(receivingThread);
        verify(endPointListenerMock, times(1)).onEndpointInfoReceived(
                new UserEndpoint("id", InetAddress.getByName("1.1.1.1"))
        );
    }

    @Test
    public void shouldNotifyEndPointListenerAboutGroup() throws UnknownHostException, SocketException, SocketTimeoutException {
        UdpBroadcastSession.EndPointListener endPointListenerMock = mock(UdpBroadcastSession.EndPointListener.class);
        final Message message = TConsts.GROUP_PEER_MESSAGE;
        udpBroadcastSession.setEndpointListener(endPointListenerMock);
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        TUtils.oneTask(receivingThread);
        verify(endPointListenerMock, times(1)).onEndpointInfoReceived(TConsts.GROUP_ENDPOINT);
    }

    @Test
    public void shouldNotifyAboutPotentialSession() throws UnknownHostException, SocketException, SocketTimeoutException {
        UdpBroadcastSession.EndPointListener endPointListenerMock = mock(UdpBroadcastSession.EndPointListener.class);
        final Message message = new Message.Builder()
                .setCommand(Message.Command.INVITE)
                .setId("id")
                .setAddressFrom("1.1.1.1")
                .build();
        udpBroadcastSession.setEndpointListener(endPointListenerMock);
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        TUtils.oneTask(receivingThread);
        verify(endPointListenerMock).onSessionInvitationReceived(
                new UserEndpoint("id", InetAddress.getByName("1.1.1.1"))
        );
    }

}
