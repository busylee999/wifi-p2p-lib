package com.busylee.network;

import com.busylee.network.message.Message;
import com.busylee.network.module.TestBuilder;
import com.busylee.network.session.EndpointSession;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UdpEndpointSession;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TLoop;
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
import java.util.List;

import javax.inject.Inject;

import static com.busylee.network.TConsts.GROUP_PEER_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 04.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkManagerTest {

    NetworkManager.Listener networkListenerMock;
    @Inject
    NetworkManager networkManager;
    @Inject
    SessionManager sessionManager;
    @Inject
    NetworkEngine networkEngine;
    @Inject
    TLoop loop;
    @Inject
    UdpEngine udpEngineMock;

    @Before
    public void setup() {
        networkListenerMock = mock(NetworkManager.Listener.class);
        new TestBuilder().build().inject(this);
        networkManager.registerNetworkListener(networkListenerMock);
    }

    @Test
    public void shouldSaveIncomingPeer() throws UnknownHostException, SocketException, SocketTimeoutException {
        String address = "1.1.1.1";
        String id = "123124315refd";
        UserEndpoint userEndpoint = new UserEndpoint(id, InetAddress.getByName(address));
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setAddressFrom(address)
                .setId(id)
                .build();
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        networkManager.start();
        loop.loop(3);
        Assert.assertThat("Should contain peer",
                networkManager.getAvailableEndpoints().contains(userEndpoint));
    }

    @Test
    public void shouldNotDuplicatePeers() throws UnknownHostException, SocketException, SocketTimeoutException {
        String address = "1.1.1.1";
        String id = "123124315refd";
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setAddressFrom(address)
                .setId(id)
                .build();
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        networkManager.start();
        loop.loop(3);
        int size = networkManager.getAvailableEndpoints().size();
        Assert.assertThat("Should contain only one peer, but size is " + size,
                size == 1);
    }

    @Test
    public void shouldStartAndStopEngine() {
        NetworkEngine networkEngineSpy = spy(this.networkEngine);
        networkManager  = new NetworkManager(networkEngineSpy, sessionManager);
        networkManager.start();
        verify(networkEngineSpy).start();
        networkManager.stop();
        verify(networkEngineSpy).stop();
    }

    @Test
    public void shouldNotifyListenerAboutPeer() throws UnknownHostException, SocketException, SocketTimeoutException {
        String address = "1.1.1.1";
        String id = "123124315refd";
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setAddressFrom(address)
                .setId(id)
                .build();
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        networkManager.start();
        loop.loop(3);
        verify(networkListenerMock).onPeerChanged();
    }

    @Test
    public void shouldStartSessionManager() {
        SessionManager sessionManagerSpy = spy(this.sessionManager);
        networkManager = new NetworkManager(networkEngine, sessionManagerSpy);
        networkManager.start();
        verify(sessionManagerSpy).start();
        networkManager.stop();
        verify(sessionManagerSpy).stop();
    }

    @Test
    public void shouldCreateAndRegisterNewSession() throws SocketException, SocketTimeoutException, UnknownHostException {
        Message message = TConsts.SESSION_INVITE_MESSAGE;
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        networkManager.start();
        loop.loop(3);
        UdpEndpointSession udpEndpointSession
                = (UdpEndpointSession) sessionManager.getSessionList().get(0);

        Assert.assertEquals(TConsts.INVITE_ENDPOINT, udpEndpointSession.getEndpoint());
    }

    @Test
    public void shouldNotCreateDuplicateSession() throws UnknownHostException, SocketException, SocketTimeoutException {
        Message message = TConsts.SESSION_INVITE_MESSAGE;
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        networkManager.start();
        loop.loop(3);
        networkManager.createSession(new UserEndpoint(null, TConsts.ADDRESS));
        Assert.assertThat("Should not duplicate sessions", sessionManager.getSessionList().size() == 1);
    }

    @Test
    public void shouldDetectGroupPeer() throws UnknownHostException, SocketException, SocketTimeoutException {
        Message message = TConsts.GROUP_PEER_MESSAGE;
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(message));
        networkManager.start();
        loop.loop(3);
        List<Endpoint> availablePeers = networkManager.getAvailableEndpoints();
        Assert.assertTrue("Manager must contains group peer",
                availablePeers.contains(TConsts.GROUP_ENDPOINT));
    }

    @Test
    public void shouldCreateSessionForGroupEndpoint() throws SocketException, SocketTimeoutException {
        Message groupPeerMessage = TConsts.GROUP_PEER_MESSAGE;
        when(udpEngineMock.waitForNextMessage())
                .thenReturn(TUtils.toBytes(groupPeerMessage));
        networkManager.start();
        loop.loop(3);
        Assert.assertThat("Should create and store session for group endpoint",
                sessionManager.getSessionList().size() == 1);
    }

    @Test
    public void shouldNotifyAboutNewMessage() throws SocketException, SocketTimeoutException {
        Message groupPeerMessage = TConsts.GROUP_PEER_MESSAGE;
        Message dataMessage = TConsts.GROUP_DATA_MESSAGE;
        when(udpEngineMock.waitForNextMessage())
                .thenReturn(TUtils.toBytes(groupPeerMessage))
                .thenReturn(TUtils.toBytes(dataMessage));
        networkManager.start();
        loop.loop(2);
        verify(networkListenerMock)
                .onMessageReceived(TConsts.GROUP_ENDPOINT, dataMessage.getData());
    }

    @Test
    public void shouldSendMessage() {
        String message = "testmessage";
        GroupEndpoint groupEndpoint = TConsts.GROUP_ENDPOINT;
        EndpointSession sessionSpy = mock(EndpointSession.class);
        SessionManager sessionManagerSpy = spy(this.sessionManager);
        networkManager = new NetworkManager(networkEngine, sessionManagerSpy);
        when(sessionManagerSpy.getSessionByEndpoint(groupEndpoint))
                .thenReturn(sessionSpy);
        networkManager.sendMessage(groupEndpoint, message);
        verify(sessionSpy).sendDataMessage(message);
    }

    @Test
    public void shouldReturnFalseOnUnknownEndpoint() {
        String message = "testmessage";
        GroupEndpoint groupEndpoint = TConsts.GROUP_ENDPOINT;
        SessionManager sessionManagerSpy = spy(this.sessionManager);
        networkManager = new NetworkManager(networkEngine, sessionManagerSpy);
        when(sessionManagerSpy.getSessionByEndpoint(groupEndpoint))
                .thenReturn(null);
        Assert.assertThat("Should return false if there is no known endpoint",
                !networkManager.sendMessage(groupEndpoint, message));
    }

    @Test
    public void shouldSendPeerInfo() throws SocketException {
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setId(String.valueOf(GROUP_PEER_ID))
                .build();
        networkManager.start();
        networkManager.createGroup(String.valueOf(GROUP_PEER_ID));
        loop.loop(3);
        verify(udpEngineMock).sendMessage(TUtils.toBytes(message));
    }

    @Test
    public void shouldCallEndpointChangeListenerIfEndpointBecameUnavailable()
            throws UnknownHostException, SocketException, SocketTimeoutException, InterruptedException {
        String address = "1.1.1.1";
        String id = "123124315refd";
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setAddressFrom(address)
                .setId(id)
                .build();
        when(udpEngineMock.waitForNextMessage())
                .thenReturn(TUtils.toBytes(message))
                .thenThrow(new SocketException());
        networkManager.setEndpointLifeTime(300);
        networkManager.start();
        loop.loop(2);
        //wait until endpoint will be expired
        Thread.sleep(500);
        loop.loop(2);
        verify(networkListenerMock, times(2)).onPeerChanged();
    }
}
