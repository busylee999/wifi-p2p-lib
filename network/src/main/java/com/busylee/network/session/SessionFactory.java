package com.busylee.network.session;

import com.busylee.network.NetworkEngine;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;

/**
 * Created by busylee on 23.08.16.
 */

public class SessionFactory {

    public UdpBroadcastSession createSession(NetworkEngine networkEngine) {
        return new UdpBroadcastSession(networkEngine);
    }

    public UserUdpEndpointSession createSession(UserEndpoint userEndpoint, NetworkEngine networkEngine) {
        return new UserUdpEndpointSession(userEndpoint, networkEngine);
    }

    public GroupUdpEndpointSession createSession(GroupEndpoint groupEndpoint, NetworkEngine networkEngine) {
        return new GroupUdpEndpointSession(groupEndpoint, networkEngine);
    }
}
