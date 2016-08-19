package com.busylee.network.session;

import com.busylee.network.message.Message;

/**
 * Created by busylee on 03.08.16.
 */
public abstract class AbstractSession {
    enum EState {
        Handshake,
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
        void onSessionEstablished(AbstractSession abstractSession);
        void onSessionClosed(AbstractSession abstractSession);
        void onNewMessage(String data);
    }

    public class Handshake {
        public void onMessage(Message message) {

        }
    }
}
