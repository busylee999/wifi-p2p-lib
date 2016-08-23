package com.busylee.network.session;

import com.busylee.network.Assert;
import com.busylee.network.NetworkEngine;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;

/**
 * Created by busylee on 23.08.16.
 */
@RunWith(RobolectricTestRunner.class)
public class SessionFactoryTest {

    private SessionFactory sessionFactory;
    private NetworkEngine networkEngine;
    private UserEndpoint userEndpoint;
    private GroupEndpoint groupEndpoint;

    @Before
    public void setUp() throws Exception {
        sessionFactory = new SessionFactory();
        networkEngine = mock(NetworkEngine.class);
        userEndpoint = mock(UserEndpoint.class);
        groupEndpoint = mock(GroupEndpoint.class);
    }

    @Test
    public void canCreateSession() {
        Assert.assertTrue(sessionFactory.createSession(networkEngine) instanceof UdpBroadcastSession);
    }

    @Test
    public void canCreateUserSession() {
        Assert.assertTrue(
                sessionFactory.createSession(userEndpoint, networkEngine)
                        instanceof UserUdpEndpointSession);
    }

    @Test
    public void canCreateGroupSession() {
        /*Assert.assertTrue(
                sessionFactory.createSession(groupEndpoint, networkEngineMock)
                        instanceof G);
        ;*/
    }

}
