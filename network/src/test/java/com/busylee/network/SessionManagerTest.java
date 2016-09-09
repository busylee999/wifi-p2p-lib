package com.busylee.network;

import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;

import com.busylee.network.module.Mocked;
import com.busylee.network.module.TestBuilder;
import com.busylee.network.module.TestNetworkComponent;
import com.busylee.network.session.AbstractSession;
import com.busylee.network.session.EndpointSession;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UserUdpEndpointSession;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.testutils.TUtils;

import org.junit.*;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by busylee on 04.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class SessionManagerTest extends Assert {

    @Inject @Named("ping")
    HandlerThread pingThread;
    @Inject
    SessionManager sessionManager;
    @Inject @Mocked
    NetworkEngine networkEngineMock;
    private TestNetworkComponent testComponent;

    @Before
    public void setup() {
        testComponent = new TestBuilder().build();
        testComponent.inject(this);
        sessionManager.start();
    }

    @Test
    public void shouldInvokePingOnSessions() {
        EndpointSession abstractSession1 = mock(EndpointSession.class);
        EndpointSession abstractSession2 = mock(EndpointSession.class);
        sessionManager.registerSession(abstractSession1);
        sessionManager.registerSession(abstractSession2);
        TUtils.oneTask(pingThread);
        verify(abstractSession1).ping();
        verify(abstractSession2).ping();
    }

    @Test
    public void shouldReturnSavedSessionByEndpoint() throws UnknownHostException {
        UserEndpoint userEndpoint = createUserEndpoint();
        EndpointSession abstractSession = createUdpEndpointSession(userEndpoint, networkEngineMock);
        sessionManager.registerSession(abstractSession);
        assertEquals(abstractSession, sessionManager.getSessionByEndpoint(userEndpoint));
    }

    @NonNull
    private EndpointSession createUdpEndpointSession(UserEndpoint userEndpoint, NetworkEngine networkEngineMock) {
        return testComponent.getSessionFactory().createSession(userEndpoint, networkEngineMock);
    }

    @NonNull
    private UserEndpoint createUserEndpoint() throws UnknownHostException {
        return new UserEndpoint("id", InetAddress.getByName("1.1.1.1"));
    }

    @Test
    public void shouldNotRegisterSessionsForSameEndPoint() throws UnknownHostException {
        UserEndpoint userEndpoint = createUserEndpoint();
        sessionManager.registerSession(createUdpEndpointSession(userEndpoint, networkEngineMock));
        assertEquals(false, sessionManager.registerSession(createUdpEndpointSession(userEndpoint, networkEngineMock)));

        assertEquals("should skip second session", 1, sessionManager.getSessionList().size());
    }

    @Test
    public void shouldCloseAndRemoveExpiredSession() {
        UserUdpEndpointSession endpointSessionMock = mock(UserUdpEndpointSession.class);
        when(endpointSessionMock.isExpired()).thenReturn(true);
        when(endpointSessionMock.getState()).thenReturn(AbstractSession.EState.Closed);
        sessionManager.registerSession(endpointSessionMock);
        TUtils.oneTask(pingThread);
        verify(endpointSessionMock).close();
        Assert.assertTrue("Should remove session from active session list",
                sessionManager.getSessionList().size() == 0);
    }
}
