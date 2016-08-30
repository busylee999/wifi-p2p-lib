package com.busylee.network.system;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.NetworkEngine;
import com.busylee.network.NetworkManager;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.endpoint.Endpoint;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by busylee on 30.08.16.
 * Play with case in fake real situation with only
 */
@RunWith(RobolectricTestRunner.class)
public class SystemCase {

    private NetworkManager networkManager1;
    private NetworkManager networkManager2;
    private NetworkManager.Listener networkListenerMock;
    private HandlerThread sessionThread1;
    private HandlerThread sessionThread2;
    private HandlerThread sendingThread1;
    private HandlerThread sendingThread2;
    private HandlerThread receivingThread1;
    private HandlerThread receivingThread2;

    @Before
    public void setUp() throws Exception {
        FakeDisturbingTool fakeDisturbingTool = new FakeDisturbingTool();
        sessionThread1 = new HandlerThread("SessionThread1", Process.THREAD_PRIORITY_BACKGROUND);
        sessionThread2 = new HandlerThread("SessionThread2", Process.THREAD_PRIORITY_BACKGROUND);
        sendingThread1 = new HandlerThread("sendingThread1", Process.THREAD_PRIORITY_BACKGROUND);
        sendingThread2 = new HandlerThread("sendingThread2", Process.THREAD_PRIORITY_BACKGROUND);
        receivingThread1 = new HandlerThread("receivingThread1", Process.THREAD_PRIORITY_BACKGROUND);
        receivingThread2 = new HandlerThread("receivingThread2", Process.THREAD_PRIORITY_BACKGROUND);
        networkManager1 = createNetworkManager(
                sessionThread1,
                sendingThread1,
                receivingThread1,
                fakeDisturbingTool, "1.1.1.1");
        networkListenerMock = mock(NetworkManager.Listener.class);
        networkManager2 = createNetworkManager(
                sessionThread2,
                sendingThread2,
                receivingThread2, fakeDisturbingTool, "1.1.1.2");
        networkManager2.setNetworkListener(networkListenerMock);
    }

    private NetworkManager createNetworkManager(HandlerThread sessionThread,
                                                HandlerThread sendingThread,
                                                HandlerThread receivingThread,
                                                FakeDisturbingTool tool,
                                                String address) throws UnknownHostException {
        UdpEngineFakeImpl udpEngineFake = new UdpEngineFakeImpl(tool, InetAddress.getByName(address));
        NetworkEngine networkEngine = new NetworkEngine(udpEngineFake, sendingThread, receivingThread);
        SessionManager sessionManager = new SessionManager(sessionThread, networkEngine);
        return new NetworkManager(networkEngine, sessionManager);
    }

    @Test
    public void canCommunicateBetweenManagers() throws InterruptedException {
        networkManager1.start();
        networkManager2.start();

        networkManager2.createGroup();
        loop(100);
        Endpoint endpoint = networkManager1.getAvailablePeers().get(0);
        networkManager1.sendMessage(endpoint, "hellow");
        loop(100);
        verify(networkListenerMock).onMessageReceived(endpoint, "hellow");

    }
    
    void loop(int count) {
        for(int i = 0 ; i < count ; i++) {
            TUtils.oneTask(sessionThread1);
            TUtils.oneTask(sessionThread2);
            TUtils.oneTask(sendingThread1);
            TUtils.oneTask(sendingThread2);
            TUtils.oneTask(receivingThread1);
            TUtils.oneTask(receivingThread2);

        }
    }

    class FakeDisturbingTool {
        List<UdpEngineFakeImpl> udpEngineFakes = new ArrayList<>();

        void register(UdpEngineFakeImpl udpEngineFake) {
            udpEngineFakes.add(udpEngineFake);
        }

        synchronized void sendMessage(String message) {
            for(UdpEngineFakeImpl udpEngineFake : udpEngineFakes) {
                udpEngineFake.addToQueue(message);
            }
        }
    }

    class UdpEngineFakeImpl implements UdpEngine {

        final Queue<String> messagesQueue;
        final FakeDisturbingTool fake;
        final InetAddress address;

        UdpEngineFakeImpl(FakeDisturbingTool fake, InetAddress address) {
            messagesQueue = new ArrayDeque<>();
            this.address = address;
            this.fake = fake;
            this.fake.register(this);
        }

        public synchronized void addToQueue(String message) {
            messagesQueue.add(message);
        }

        @Override
        public synchronized String waitForNextMessage() throws SocketException, SocketTimeoutException {
            return messagesQueue.poll();
        }

        @Override
        public boolean sendMessage(String message) throws SocketException {
            fake.sendMessage(message);
            return true;
        }

        @Override
        public InetAddress getIpAddress() {
            return address;
        }
    }
}
