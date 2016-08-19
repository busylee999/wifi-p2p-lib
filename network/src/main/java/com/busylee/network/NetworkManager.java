package com.busylee.network;

import com.busylee.network.session.AbstractSession;
import com.busylee.network.session.EndpointSession;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UdpBroadcastSession;
import com.busylee.network.session.UdpEndpointSession;
import com.busylee.network.session.endpoint.UserEndpoint;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by busylee on 03.08.16.
 */
public class NetworkManager implements UdpBroadcastSession.EndPointListener {

    private final NetworkEngine networkEngine;
    private final UdpBroadcastSession udpBroadcastSession;
    private final SessionManager sessionManager;

    private List<UserEndpoint> userEndpoints = new ArrayList<>();
    private Listener netListener;

    public NetworkManager(NetworkEngine networkEngine, SessionManager sessionManager) {
        this.networkEngine = networkEngine;
        this.udpBroadcastSession = new UdpBroadcastSession(networkEngine);
        this.udpBroadcastSession.setEndpointListener(this);
        this.sessionManager = sessionManager;
    }

    public List<UserEndpoint> getAvailablePeers() {
        return userEndpoints;
    }

    @Override
    public void onPotentialSessionReceived(UserEndpoint userEndpoint) {
        sessionManager.registerSession(new UdpEndpointSession(userEndpoint, networkEngine));
    }

    @Override
    public void onEndpointInfoReceived(UserEndpoint endpoint) {
        if(!userEndpoints.contains(endpoint)) {
            userEndpoints.add(endpoint);
            if(netListener != null) {
                netListener.onPeerChanged();
            }
        }
    }

    public AbstractSession createSession(UserEndpoint userEndpoint) {
        EndpointSession abstractSession = sessionManager.getSessionByEndpoint(userEndpoint);
        if(abstractSession == null) {
            abstractSession = new UdpEndpointSession(userEndpoint, networkEngine);
            sessionManager.registerSession(abstractSession);
        }

        return abstractSession;
    }

    public void start() {
        networkEngine.start();
        sessionManager.start();
    }

    public void stop() {
        networkEngine.stop();
        sessionManager.stop();
    }

    public InetAddress getIpAddress() {
        return networkEngine.getIpAddress();
    }

    public void setNetworkListener(Listener netListener) {
        this.netListener = netListener;
    }

    public interface Listener {
        void onPeerChanged();
    }
}
