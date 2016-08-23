package com.busylee.network.session;

import com.busylee.network.session.endpoint.Endpoint;

/**
 * Created by busylee on 04.08.16.
 */
//TODO seems deprecated?
public abstract class EndpointSession extends AbstractSession {
    public abstract Endpoint getEndpoint();
}
