package com.busylee.network.session;

import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;

import com.busylee.network.NetworkEngine;
import com.busylee.network.TConsts;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TUtils;
import com.busylee.network.udp.UdpEngine;

import org.junit.Assert;
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
 * Created by busylee on 23.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class UserUdpEndpointSessionTest {

    NetworkEngine networkEngine;
    HandlerThread sendingThread;
    HandlerThread receivingThread;
    UdpEngine udpEngineMock;
    UserUdpEndpointSession udpEndpointSession;
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
        udpEndpointSession = createEndpointSession();
        udpEndpointSession.setSessionListener(sessionListenerMock);
        networkEngine.start();

    }

    @Test
    public void shouldSendDirectMessage() {
        final String message = "testMessage";
        networkEngine = spy(networkEngine);
        udpEndpointSession = createEndpointSession();
        udpEndpointSession.sendDataMessage(message);
        verify(networkEngine).sendMessageBroadcast(
                "{\"addressTo\":\"1.1.1.1\"," +
                        "\"id\":\"id\"," +
                        "\"command\":\"DATA\"," +
                        "\"data\":\"testMessage\"}"
        );
    }

    @NonNull
    private UserUdpEndpointSession createEndpointSession() {
        return new SessionFactory().createSession(userEndpoint, networkEngine);
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
        verify(sessionListenerMock).onNewMessage(userEndpoint, data);
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
        verify(sessionListenerMock, never()).onNewMessage(userEndpoint, data);
    }

    @Test
    public void shouldIgnoreMessagesFromAnotherIdsWithExistingOne() throws UnknownHostException, SocketException, SocketTimeoutException {
        String data = "tratata";
        Message message = createIncomingMessage(data);
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        TUtils.oneTask(receivingThread);
        verify(sessionListenerMock, never()).onNewMessage(userEndpoint, data);
    }

    private Message createIncomingMessage(String data) throws UnknownHostException {
        return new Message.Builder()
                    .setAddressFrom("1.1.1.1")
                    .setCommand(Message.Command.DATA)
                    .setId("1434")
                    .setData(data)
                    .build();
    }

    @Test
    public void shouldNotExpired() {
        Message message = TConsts.GROUP_DATA_MESSAGE;
        udpEndpointSession.update(null, message.toString());
        Assert.assertTrue("Should not be expired", !udpEndpointSession.isExpired());
    }

    @Test
    public void shouldExpiredSimultaneously() {
        udpEndpointSession =
                new UserUdpEndpointSession(userEndpoint, networkEngine, 0);
        Assert.assertTrue("Should be expired", udpEndpointSession.isExpired());
    }

    @Test
    public void shouldGoToClosedState() {
        udpEndpointSession.close();
        Assert.assertEquals("Should go to correct closed state after close",
                EndpointSession.EState.Closed, udpEndpointSession.getState());
    }
}
