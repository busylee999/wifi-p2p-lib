package com.busylee.network.session;

import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.Endpoint;

/**
 * Created by busylee on 03.08.16.
 */
public abstract class AbstractSession {
    enum EState {
        Established,
        Closed
    }

    public abstract EState getState();
    public abstract void close();
    public abstract void ping();
    public abstract void sendMessage(String message);
    public abstract void setSessionListener(SessionListener sessionListener);
    abstract void sendMessage(Message message);

    public interface SessionListener {
        void onNewMessage(Endpoint endpoint, String data);
    }
}
