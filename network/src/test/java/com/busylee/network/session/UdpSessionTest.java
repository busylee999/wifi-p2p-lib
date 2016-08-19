package com.busylee.network.session;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.Network;
import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TUtils;
import com.busylee.network.udp.UdpEngine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 04.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class UdpSessionTest {

    NetworkEngine networkEngine;
    HandlerThread sendingThread;
    HandlerThread receivingThread;
    UdpEngine udpEngineMock;
    UdpEndpointSession udpEndpointSession;
    UserEndpoint userEndpoint;

    AbstractSession.SessionListener sessionListenerMock;

    @Before
    public void setup() throws UnknownHostException {
        sessionListenerMock = mock(AbstractSession.SessionListener.class);
        userEndpoint = new UserEndpoint("id", InetAddress.getByName("1.1.1.1"));
        udpEngineMock = mock(UdpEngine.class);
        sendingThread
                = new HandlerThread("SendingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        receivingThread
                = new HandlerThread("ReceivingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        networkEngine = new NetworkEngine(udpEngineMock, sendingThread, receivingThread);
        udpEndpointSession = new UdpEndpointSession(userEndpoint, networkEngine);
        udpEndpointSession.setSessionListener(sessionListenerMock);
        networkEngine.addObserver(udpEndpointSession);
        networkEngine.start();

    }

    @Test
    public void shouldSendDirectMessage() {
        final String message = "testMessage";
        networkEngine = spy(networkEngine);
        udpEndpointSession = new UdpEndpointSession(userEndpoint, networkEngine);
        udpEndpointSession.sendMessage(message);
        verify(networkEngine).sendMessageBroadcast(
                "{\"addressTo\":\"1.1.1.1\"," +
                "\"id\":\"id\"," +
                "\"command\":\"DATA\"," +
                "\"data\":\"testMessage\"}"
        );
    }

    @Test
    public void shouldNotifyListenerOnNewDataMessage() throws UnknownHostException, SocketException, SocketTimeoutException {
        String data = "tratata";
        Message message = new Message.Builder()
                .setAddressFrom("1.1.1.1")
                .setCommand(Message.Command.DATA)
                .setData(data)
                .build();
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        TUtils.oneTask(receivingThread);
        verify(sessionListenerMock).onNewMessage(data);
    }

    @Test
    public void shouldIgnoreMessagesFromAnotherEndpoints() throws UnknownHostException, SocketException, SocketTimeoutException {
        String data = "tratata";
        Message message = new Message.Builder()
                .setAddressFrom("1.1.2.2")
                .setCommand(Message.Command.DATA)
                .setData(data)
                .build();
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        TUtils.oneTask(receivingThread);
        verify(sessionListenerMock, never()).onNewMessage(data);
    }

    @Test
    public void shouldIgnoreMessagesFromAnotherIdsWithExistingOne() throws UnknownHostException, SocketException, SocketTimeoutException {
        String data = "tratata";
        Message message = new Message.Builder()
                .setAddressFrom("1.1.1.1")
                .setCommand(Message.Command.DATA)
                .setId("1434")
                .setData(data)
                .build();
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        TUtils.oneTask(receivingThread);
        verify(sessionListenerMock, never()).onNewMessage(data);
    }
}
