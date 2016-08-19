package com.busylee.network.session;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.session.endpoint.UserEndpoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by busylee on 04.08.16.
 */
public class SessionManager implements Handler.Callback {

    private static final String TAG = "SessionManager";

    public synchronized EndpointSession getSessionByEndpoint(UserEndpoint userEndpoint) {
        for(EndpointSession abstractSession: sessionList) {
            if(abstractSession.getEndpoint().getAddress().equals(userEndpoint.getAddress())) {
                return abstractSession;
            }
        }
        return null;
    }

    enum State {
        Idle,
        Running,
        Stopped
    }

    private final HandlerThread handlerThread;
    private List<EndpointSession> sessionList = new ArrayList<>();
    private Handler handler;
    private final int delay;
    private State mState = State.Idle;

    public SessionManager(HandlerThread handlerThread, NetworkEngine networkEngine) {
        this(handlerThread, networkEngine, 500);
    }

    public SessionManager(HandlerThread handlerThread, NetworkEngine networkEngine, int delay) {
        this.handlerThread = handlerThread;
        this.delay = delay;
    }

    public synchronized void registerSession(EndpointSession abstractSession) {
        if(!sessionList.contains(abstractSession)) {
            sessionList.add(abstractSession);
        } else {
            Log.w(TAG, "Session already in list session = " + abstractSession);
        }
    }

    private void pingSessions() {
        Message message = new Message();
        this.handler.sendMessageDelayed(message, delay);
    }

    private synchronized void onPingSessions() {
        Iterator<EndpointSession> iterator = sessionList.iterator();
        while(iterator.hasNext()) {
            AbstractSession abstractSession = iterator.next();
            if(abstractSession.getState() == AbstractSession.EState.Closed) {
                iterator.remove();
            }
            abstractSession.ping();
        }

        pingSessions();
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

        pingSessions();
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
}
