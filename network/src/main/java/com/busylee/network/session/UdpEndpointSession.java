package com.busylee.network.session;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.Endpoint;

import java.util.Observer;


/**
 * Created by busylee on 04.08.16.
 */
public abstract class UdpEndpointSession extends EndpointSession implements Observer {

    protected final NetworkEngine networkEngine;
    private final Endpoint endpoint;
    protected SessionListener sessionListener;
    private EState mCurrentState;

    public UdpEndpointSession(Endpoint endpoint, NetworkEngine networkEngine) {
        this.endpoint = endpoint;
        this.networkEngine = networkEngine;
        this.networkEngine.addObserver(this);
        //TODO think about it
        mCurrentState = EState.Established;
    }

    public abstract void ping();

    @Override
    public EState getState() {
        return mCurrentState;
    }

    @Override
    public void close() {
        mCurrentState = EState.Closed;
    }

    @Override
    public void sendMessage(Message message) {
        networkEngine.sendMessageBroadcast(message.toString().getBytes());
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
