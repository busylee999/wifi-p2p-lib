package com.busylee.network;

import android.os.HandlerThread;
import android.os.Process;

import com.busylee.network.session.AbstractSession;
import com.busylee.network.session.EndpointSession;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UdpEndpointSession;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by busylee on 04.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class SessionManagerTest {

    HandlerThread pingThread;
    SessionManager sessionManager;
    NetworkEngine networkEngineMock;
    @Before
    public void setup() {
        networkEngineMock = mock(NetworkEngine.class);
        pingThread
                = new HandlerThread("PingThreadTest", Process.THREAD_PRIORITY_BACKGROUND);

        sessionManager = new SessionManager(pingThread, networkEngineMock);
        sessionManager.start();
    }

    @Test
    public void shouldInvokePingOnSession() {
        EndpointSession abstractSession = mock(EndpointSession.class);
        sessionManager.registerSession(abstractSession);
        TUtils.oneTask(pingThread);
        verify(abstractSession).ping();
    }

    @Test
    public void shouldReturnSavedSessionByEndpoint() throws UnknownHostException {
        UserEndpoint userEndpoint = new UserEndpoint("id", InetAddress.getByName("1.1.1.1"));
        sessionManager.registerSession(new UdpEndpointSession(
                userEndpoint, networkEngineMock));
        EndpointSession sessionByEndpoint = sessionManager.getSessionByEndpoint(userEndpoint);
        sessionByEndpoint.getEndpoint().equals(userEndpoint);
    }

}
