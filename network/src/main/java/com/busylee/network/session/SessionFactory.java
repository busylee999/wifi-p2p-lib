package com.busylee.network.session;

import com.busylee.network.Logger;
import com.busylee.network.NetworkEngine;
import com.busylee.network.serialization.SerializationContext;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;

import javax.inject.Inject;

/**
 * Created by busylee on 23.08.16.
 */
public class SessionFactory {

    private final SerializationContext serializationContext;
    private final Logger logger;

    @Inject
    public SessionFactory(SerializationContext serializationContext, Logger logger) {
        this.serializationContext = serializationContext;
        this.logger = logger;
    }

    public EndpointSession createSession(Endpoint endpoint, NetworkEngine networkEngine) {
        if(endpoint instanceof GroupEndpoint) {
            return createSession((GroupEndpoint) endpoint, networkEngine, logger);
        } else {
            return createSession((UserEndpoint) endpoint, networkEngine);
        }
    }

    public UdpBroadcastSession createSession(NetworkEngine networkEngine) {
        return new UdpBroadcastSession(networkEngine, serializationContext);
    }

    public UserUdpEndpointSession createSession(UserEndpoint userEndpoint, NetworkEngine networkEngine) {
        return new UserUdpEndpointSession(userEndpoint, networkEngine, serializationContext);
    }

    public GroupUdpEndpointSession createSession(GroupEndpoint groupEndpoint, NetworkEngine networkEngine, Logger logger) {
        return new GroupUdpEndpointSession(groupEndpoint, networkEngine, serializationContext, logger);
    }
}
