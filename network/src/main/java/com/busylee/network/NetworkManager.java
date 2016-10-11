package com.busylee.network;

import com.busylee.network.message.Message;
import com.busylee.network.serialization.Base64Context;
import com.busylee.network.session.AbstractSession;
import com.busylee.network.session.EndpointSession;
import com.busylee.network.session.SessionFactory;
import com.busylee.network.session.SessionManager;
import com.busylee.network.session.UdpBroadcastSession;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.busylee.network.utils.AndroidLogger;
import com.google.gson.GsonBuilder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by busylee on 03.08.16.
 */
public class NetworkManager implements UdpBroadcastSession.EndPointListener, AbstractSession.SessionListener, SessionManager.OnPingLoopListener {

    private static final String TAG = "NetworkManager";

    public final int ENDPOINT_AVAILABLE_DEFAULT_TIME = 4 * 60 * 1000;

    private final NetworkEngine networkEngine;
    private final Logger logger;
    private final UdpBroadcastSession udpBroadcastSession;
    private final SessionManager sessionManager;
    private final SessionFactory sessionFactory;

    private List<Endpoint> knownEndpoint = new ArrayList<>();
    private Map<String, Long> knownEndpointsLastActionTime = new HashMap<>();

    private final Set<Listener> listeners = new HashSet<>();
    private int endpointLifeTime = ENDPOINT_AVAILABLE_DEFAULT_TIME;

    public NetworkManager(NetworkEngine networkEngine, SessionManager sessionManager) {
        this(networkEngine, sessionManager, new AndroidLogger());
    }

    public NetworkManager(NetworkEngine networkEngine, SessionManager sessionManager, Logger logger) {
        this(networkEngine, sessionManager, new SessionFactory(new Base64Context(new GsonBuilder().create()), logger), logger);
    }

    @Inject
    public NetworkManager(NetworkEngine networkEngine, SessionManager sessionManager, SessionFactory sessionFactory, Logger logger) {
        this.sessionFactory = sessionFactory;
        this.networkEngine = networkEngine;
        this.logger = logger;
        this.udpBroadcastSession = sessionFactory.createSession(networkEngine);
        this.udpBroadcastSession.setEndpointListener(this);
        this.sessionManager = sessionManager;
        this.sessionManager.setOnPingListener(this);
    }

    public List<Endpoint> getAvailableEndpoints() {
        return knownEndpoint;
    }

    @Override
    public void onSessionInvitationReceived(UserEndpoint userEndpoint) {
        sessionManager.registerSession(sessionFactory.createSession(userEndpoint, networkEngine));
    }

    private void updateEndpointExpiredTime(Endpoint endpoint) {
        String id = getEndpointUUID(endpoint);
        knownEndpointsLastActionTime.put(id, System.currentTimeMillis() + endpointLifeTime);
    }

    private String getEndpointUUID(Endpoint endpoint) {
        String id = "";
        if(endpoint instanceof UserEndpoint) {
            id = ((UserEndpoint) endpoint).getAddress().toString() + "user";
        }
        if(endpoint instanceof GroupEndpoint) {
            id = ((GroupEndpoint) endpoint).getId();
        }
        return id;
    }

    @Override
    public synchronized void onEndpointInfoReceived(Endpoint endpoint) {
        synchronized (knownEndpoint) {
            if(!knownEndpoint.contains(endpoint)) {
                knownEndpoint.add(endpoint);
                notifyListenersAboutPeersChanged();

                if(endpoint instanceof GroupEndpoint) {
                    createSession(endpoint);
                }
            }

            updateEndpointExpiredTime(endpoint);
        }
    }

    private void notifyListenersAboutPeersChanged() {
        synchronized (listeners) {
            for(Listener listener : listeners) {
                listener.onPeerChanged();
            }
        }
    }

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

    public void registerNetworkListener(Listener netListener) {
        if(netListener != null) {
            synchronized (listeners) {
                this.listeners.add(netListener);
            }
        }
    }

    public void unregisterNetworkListener(Listener netListener) {
        if(netListener != null) {
            this.listeners.remove(netListener);
        }
    }

    @Override
    public void onNewMessage(Endpoint endpoint, String data) {
        logger.d(TAG, data);
        notifyListenersAboutNewMessage(endpoint, data);
    }

    private void notifyListenersAboutNewMessage(Endpoint endpoint, String data) {
        synchronized (listeners) {
            for(Listener listener : listeners) {
                listener.onMessageReceived(endpoint, data);
            }
        }
    }

    public boolean sendMessage(Endpoint endpoint, String message) {
        logger.d(TAG, message);
        EndpointSession session = sessionManager.getSessionByEndpoint(endpoint);
        if(session == null && !knownEndpoint.contains(endpoint)) {
            return false;
        }

        session = createSession(endpoint);

        if(session != null) {
            session.sendDataMessage(message);
            return true;
        }

        return false;
    }

    @Deprecated
    public String createGroup() {
        String peerId = String.valueOf(new Random().nextLong());
        createGroup(peerId);
        return peerId;
    }

    public void createGroup(String peerId) {
        Message message = new Message.Builder()
                .setCommand(Message.Command.PEER)
                .setId(String.valueOf(peerId))
                .build();

        udpBroadcastSession.sendMessage(message);
    }

    void setEndpointLifeTime(int endpointLifeTime) {
        this.endpointLifeTime = endpointLifeTime;
    }

    @Override
    //TODO maybe we need to ocheck for open session
    //TODO we need to move this code for session manager
    public void pingLoop() {
        synchronized (knownEndpoint) {
            Iterator<Map.Entry<String, Long>> iterator = knownEndpointsLastActionTime.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                String endpointUUID = entry.getKey();
                Long endpointExpiredTime = entry.getValue();
                if(endpointExpiredTime < System.currentTimeMillis()) {
                    logger.d(TAG, "peer seems to be expired");
                    iterator.remove();
                    Iterator<Endpoint> endpointIterator = knownEndpoint.iterator();
                    while (endpointIterator.hasNext()) {
                        Endpoint endpoint = endpointIterator.next();
                        if(getEndpointUUID(endpoint).equals(endpointUUID)) {
                            endpointIterator.remove();
                            break;
                        }
                    }
                    notifyListenersAboutPeersChanged();
                }
            }
        }
    }

    public interface Listener {
        void onPeerChanged();
        void onMessageReceived(Endpoint groupEndpoint, String data);
    }

}
