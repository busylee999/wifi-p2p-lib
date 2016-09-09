package com.busylee.network;

import android.os.HandlerThread;

import com.busylee.network.module.Mocked;
import com.busylee.network.module.TestBuilder;
import com.busylee.network.testutils.TUtils;
import com.busylee.network.udp.UdpEngine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 02.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkEngineTest {

    @Inject
    NetworkEngine networkEngine;
    @Inject @Named("sending")
    HandlerThread sendingThread;
    @Inject @Named("receiving")
    HandlerThread receivingThread;
    @Inject
    UdpEngine udpEngineMock;
    Network.NetworkListener networkListenerMock;

    @Before
    public void setup() {
        new TestBuilder().build().inject(this);
        networkListenerMock = mock(Network.NetworkListener.class);
        networkEngine.addObserver(networkListenerMock);
        networkEngine.start();
    }

    @After
    public void tearDown() {
        networkEngine.stop();
    }

    @Test
    public void shouldStartWaitingMessage() throws SocketException, SocketTimeoutException {
        TUtils.oneTask(receivingThread);
        verify(udpEngineMock, atLeastOnce()).waitForNextMessage();
    }

    @Test
    public void shouldNotifyListener() throws SocketException, SocketTimeoutException {
        final String testMessage = "test message";
        when(udpEngineMock.waitForNextMessage()).thenReturn(TUtils.toBytes(testMessage));
        TUtils.oneTask(receivingThread);
        TUtils.oneTask(sendingThread);
        verify(networkListenerMock, times(1)).update((Observable) any(), eq(TUtils.toBytes(testMessage)));
    }

}
