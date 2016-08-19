package com.busylee.network.session;

import com.busylee.network.session.endpoint.UserEndpoint;

/**
 * Created by busylee on 04.08.16.
 */
//TODO seems deprecated?
public abstract class EndpointSession extends AbstractSession {
    public abstract UserEndpoint getEndpoint();
}
