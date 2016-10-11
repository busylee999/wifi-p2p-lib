package com.busylee.network.session;

import com.busylee.network.Logger;
import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.serialization.SerializationContext;
import com.busylee.network.serialization.SerializationListener;
import com.busylee.network.session.endpoint.Endpoint;


/**
 * Created by busylee on 04.08.16.
 */
public abstract class UdpEndpointSession extends EndpointSession implements SerializationListener {
    static final int DEFAULT_EXPIRED_BOUND = 11 * 1000; //11 sec
    protected final NetworkEngine networkEngine;
    private final SerializationContext serializationContext;
    private final Endpoint endpoint;
    protected SessionListener sessionListener;
    private EState state;

    public UdpEndpointSession(Endpoint endpoint, NetworkEngine networkEngine, SerializationContext serializationContext) {
        this.endpoint = endpoint;
        this.networkEngine = networkEngine;
        this.serializationContext = serializationContext;
        this.serializationContext.setListener(this);
        this.networkEngine.addObserver(serializationContext);
        //TODO think about it
        state = EState.Established;
    }

    public abstract void ping();

    @Override
    public EState getState() {
        return state;
    }

    @Override
    public void close() {
        state = EState.Closed;
    }

    @Override
    public void sendMessage(Message message) {
        serializationContext.sendMessage(networkEngine, message);
    }

    @Override
    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UdpEndpointSession)) {
            return false;
        }

        UdpEndpointSession udpEndpointSession = (UdpEndpointSession) o;

        if(udpEndpointSession.endpoint == null
                || !udpEndpointSession.endpoint.equals(this.endpoint)) {
            return false;
        }

        return true;
    }
}
