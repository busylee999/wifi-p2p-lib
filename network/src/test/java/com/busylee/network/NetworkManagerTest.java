package com.busylee.network;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.message.Message;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UdpEndpointSession;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TUtils;
import com.busylee.network.udp.UdpEngine;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 04.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkManagerTest {

    NetworkManager.Listener netListenerMock;
    NetworkManager networkManager;
    NetworkEngine networkEngine;
    HandlerThread pingThread;
    HandlerThread sendingThread;
    HandlerThread receivingThread;
    UdpEngine udpEngineMock;
    Network.NetworkListener networkListenerMock;
    SessionManager sessionManager;

    @Before
    public void setup() {
        pingThread
                = new HandlerThread("PingThreadThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        netListenerMock = mock(NetworkManager.Listener.class);
        udpEngineMock = mock(UdpEngine.class);
        networkListenerMock = mock(Network.NetworkListener.class);
        sendingThread
                = new HandlerThread("SendingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        receivingThread
                = new HandlerThread("ReceivingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);
        networkEngine = new NetworkEngine(udpEngineMock, sendingThread, receivingThread);
        networkEngine.addObserver(networkListenerMock);
        sessionManager = new SessionManager(pingThread, networkEngine);
        networkManager = new NetworkManager(networkEngine, sessionManager);
        networkManager.setNetworkListener(netListenerMock);
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
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        networkManager.start();
        TUtils.oneTask(receivingThread);
        Assert.assertThat("Should contain peer",
                networkManager.getAvailablePeers().contains(userEndpoint));
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
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        networkManager.start();
        TUtils.oneTask(receivingThread);
        TUtils.oneTask(receivingThread);
        Assert.assertThat("Should contain only one peer",
                networkManager.getAvailablePeers().size() == 1);
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
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        networkManager.start();
        TUtils.oneTask(receivingThread);
        TUtils.oneTask(receivingThread);
        verify(netListenerMock).onPeerChanged();
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
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        networkManager.start();
        TUtils.oneTask(receivingThread);
        UdpEndpointSession udpEndpointSession
                = (UdpEndpointSession) sessionManager.getSessionList().get(0);

        Assert.assertEquals(TConsts.INVITE_ENDPOINT, udpEndpointSession.getEndpoint());
    }

    @Test
    public void shouldNotCreateDuplicateSession() throws UnknownHostException, SocketException, SocketTimeoutException {
        Message message = TConsts.SESSION_INVITE_MESSAGE;
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        networkManager.start();
        TUtils.oneTask(receivingThread);
        networkManager.createSession(new UserEndpoint("id", InetAddress.getByName("1.1.1.1")));
        Assert.assertThat("Should not duplicate sessions", sessionManager.getSessionList().size() == 1);
    }

    @Test
    public void shouldDetectGroupPeer() throws UnknownHostException, SocketException, SocketTimeoutException {
        Message message = TConsts.GROUP_PEER_MESSAGE;
        when(udpEngineMock.waitForNextMessage()).thenReturn(message.toString());
        networkManager.start();
        TUtils.oneTask(receivingThread);
        List<Endpoint> availablePeers = networkManager.getAvailablePeers();
        Assert.assertTrue("Manager must contains group peer",
                availablePeers.contains(TConsts.GROUP_ENDPOINT));
    }
}
