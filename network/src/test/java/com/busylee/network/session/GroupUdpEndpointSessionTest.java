package com.busylee.network.session;

import com.busylee.network.NetworkEngine;
import com.busylee.network.TConsts;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by busylee on 23.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class GroupUdpEndpointSessionTest {

    NetworkEngine networkEngineMock;
    GroupUdpEndpointSession udpEndpointSession;
    GroupEndpoint groupEndpoint;

    AbstractSession.SessionListener sessionListenerMock;

    @Before
    public void setup() throws UnknownHostException {
        sessionListenerMock = mock(AbstractSession.SessionListener.class);
        groupEndpoint = TConsts.GROUP_ENDPOINT;
        networkEngineMock = mock(NetworkEngine.class);
        udpEndpointSession = new SessionFactory().createSession(groupEndpoint, networkEngineMock);
        udpEndpointSession.setSessionListener(sessionListenerMock);
        networkEngineMock.start();

    }

    @Test
    public void shouldSendDirectMessage() {
        final String message = "testMessage";
        udpEndpointSession.sendMessage(message);
        verify(networkEngineMock).sendMessageBroadcast(
                "{\"id\":\"" + TConsts.GROUP_PEER_ID + "\"," +
                        "\"command\":\"DATA\"," +
                        "\"data\":\"testMessage\"}"
        );
    }

    @Test
    public void shouldNotifyListenerOnNewDataMessage() throws UnknownHostException, SocketException, SocketTimeoutException {
        udpEndpointSession.update(null, TConsts.GROUP_DATA_MESSAGE.toString());
        verify(sessionListenerMock).onNewMessage(groupEndpoint, TConsts.GROUP_MESSAGE_TEXT);
    }

    @Test
    public void shouldIgnoreMessagesFromAnotherEndpoints() throws UnknownHostException, SocketException, SocketTimeoutException {
        String data = "tratata";
        Message message = new Message.Builder()
                .setCommand(Message.Command.DATA)
                .setData(data)
                .build();
        udpEndpointSession.update(null, message.toString());
        verify(sessionListenerMock, never()).onNewMessage((Endpoint) any(), anyString());
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
                new GroupUdpEndpointSession(TConsts.GROUP_ENDPOINT, networkEngineMock, 0);
        Assert.assertTrue("Should be expired", udpEndpointSession.isExpired());
    }
}
