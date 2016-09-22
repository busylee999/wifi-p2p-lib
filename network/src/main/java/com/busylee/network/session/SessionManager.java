package com.busylee.network.session;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.busylee.network.module.HandlerThreadModule;
import com.busylee.network.session.endpoint.Endpoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by busylee on 04.08.16.
 * TODO should notify listener about session expiring
 */
public class SessionManager implements Handler.Callback {

    private static final String TAG = "SessionManager";

    static final int PING_DEFAULT_DELAY = 500;

    enum State {
        Idle,
        Running,
        Stopped
    }

    private OnPingLoopListener onPingListener;

    private final HandlerThread handlerThread;
    private List<EndpointSession> sessionList = new ArrayList<>();
    private Handler handler;
    private final int delay;
    private State mState = State.Idle;

    public SessionManager() {
        this(new HandlerThreadModule().providePingThread());
    }

    @Inject
    public SessionManager(@Named("ping") HandlerThread handlerThread) {
        this(handlerThread, PING_DEFAULT_DELAY);
    }

    SessionManager(HandlerThread handlerThread, int delay) {
        this.handlerThread = handlerThread;
        this.delay = delay;
    }

    public synchronized EndpointSession getSessionByEndpoint(Endpoint endpoint) {
        for(EndpointSession abstractSession: sessionList) {
            if(abstractSession.getEndpoint().equals(endpoint)) {
                return abstractSession;
            }
        }
        return null;
    }

    public void setOnPingListener(OnPingLoopListener onPingLoopListener) {
        this.onPingListener = onPingLoopListener;
    }

    public synchronized boolean registerSession(EndpointSession abstractSession) {
        if(!sessionList.contains(abstractSession)) {
            return sessionList.add(abstractSession);
        }

        Log.w(TAG, "Session already in list session = " + abstractSession);

        return false;
    }

    private void schedulePingSession() {
        Message message = new Message();
        this.handler.sendMessageDelayed(message, delay);
    }

    private void onPingSessions() {
        pingSessions();
        schedulePingSession();
        if(onPingListener != null) {
            onPingListener.pingLoop();
        }
    }

    private synchronized void pingSessions() {
        Iterator<EndpointSession> iterator = sessionList.iterator();
        while(iterator.hasNext()) {
            EndpointSession endpointSession = iterator.next();
            closeOrPingSession(endpointSession);
            if(endpointSession.getState() == AbstractSession.EState.Closed) {
                iterator.remove();
            }
        }
    }

    private void closeOrPingSession(EndpointSession endpointSession) {
        if(endpointSession.isExpired()) {
            endpointSession.close();
        } else {
            endpointSession.ping();
        }
    }

    public void start() {
        Log.d(TAG, "start()");
        if(mState == State.Running) {
            Log.w(TAG, "start() already starting");
            return;
        }

        if(!handlerThread.isAlive()) {
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper(), this);
        }

        mState = State.Running;

        schedulePingSession();
    }

    public void stop() {
        Log.d(TAG, "stop()");
        if(mState != State.Running) {
            Log.w(TAG, "start() not running");
            return;
        }

        if(handlerThread.isAlive() && !handlerThread.isInterrupted()) {
            handlerThread.quit();
            handler = null;
        }

        mState = State.Stopped;

    }

    @Override
    public boolean handleMessage(Message msg) {
        onPingSessions();
        return false;
    }

    public synchronized List<EndpointSession> getSessionList() {
        return sessionList;
    }

    public interface OnPingLoopListener {
        void pingLoop();
    }

}
