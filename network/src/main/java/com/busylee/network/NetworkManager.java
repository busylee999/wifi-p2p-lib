package com.busylee.network;

import com.busylee.network.message.Message;
import com.busylee.network.session.AbstractSession;
import com.busylee.network.session.EndpointSession;
import com.busylee.network.session.SessionFactory;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UdpBroadcastSession;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by busylee on 03.08.16.
 */
public class NetworkManager implements UdpBroadcastSession.EndPointListener, AbstractSession.SessionListener {

    private final NetworkEngine networkEngine;
    private final UdpBroadcastSession udpBroadcastSession;
    private final SessionManager sessionManager;
    private final SessionFactory sessionFactory;

    private List<Endpoint> knownEndpoint = new ArrayList<>();
    private Listener netListener;

    public NetworkManager(NetworkEngine networkEngine, SessionManager sessionManager) {
        this(networkEngine, sessionManager, new SessionFactory());
    }

    public NetworkManager(NetworkEngine networkEngine, SessionManager sessionManager, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.networkEngine = networkEngine;
        this.udpBroadcastSession = sessionFactory.createSession(networkEngine);
        this.udpBroadcastSession.setEndpointListener(this);
        this.sessionManager = sessionManager;
    }

    public List<Endpoint> getAvailablePeers() {
        return knownEndpoint;
    }

    @Override
    public void onSessionInvitationReceived(UserEndpoint userEndpoint) {
        sessionManager.registerSession(sessionFactory.createSession(userEndpoint, networkEngine));
    }

    @Override
    public void onEndpointInfoReceived(Endpoint endpoint) {
        if(!knownEndpoint.contains(endpoint)) {
            knownEndpoint.add(endpoint);
            if(netListener != null) {
                netListener.onPeerChanged();
            }

            if(endpoint instanceof GroupEndpoint) {
                createSession(((GroupEndpoint) endpoint));
            }
        }
    }

//    public void sendMessage(Endpoint endpoint, String message) {
//
//    }

    public EndpointSession createSession(Endpoint endpoint) {
        EndpointSession abstractSession = sessionManager.getSessionByEndpoint(endpoint);
        if(abstractSession == null) {
            abstractSession = sessionFactory.createSession(endpoint, networkEngine);
            abstractSession.setSessionListener(this);
            sessionManager.registerSession(abstractSession);
        }

        return abstractSession;
    }

    @Deprecated
    public AbstractSession createSession(UserEndpoint userEndpoint) {
        EndpointSession abstractSession = sessionManager.getSessionByEndpoint(userEndpoint);
        if(abstractSession == null) {
            abstractSession = sessionFactory.createSession(userEndpoint, networkEngine);
            abstractSession.setSessionListener(this);
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

    @Override
    public void onSessionEstablished(AbstractSession abstractSession) {
        //TODO
    }

    @Override
    public void onSessionClosed(AbstractSession abstractSession) {
        //TODO
    }

    @Override
    public void onNewMessage(Endpoint endpoint, String data) {
        if(this.netListener != null) {
            this.netListener.onMessageReceived(endpoint, data);
        }
    }

    public boolean sendMessage(Endpoint endpoint, String message) {
        EndpointSession session = sessionManager.getSessionByEndpoint(endpoint);
        if(session == null && !knownEndpoint.contains(endpoint)) {
            return false;
        }

        session = createSession(endpoint);

        if(session != null) {
            session.sendMessage(message);
            return true;
        }

        return false;
    }

    public String createGroup() {
        String peerId = String.valueOf(new Random().nextLong());
        createGroup(peerId);
        return peerId;
    }

    void createGroup(String peerId) {
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setId(String.valueOf(peerId))
                .build();

        udpBroadcastSession.sendMessage(message);
    }

    public interface Listener {
        void onPeerChanged();
        void onMessageReceived(Endpoint groupEndpoint, String data);
    }

}
