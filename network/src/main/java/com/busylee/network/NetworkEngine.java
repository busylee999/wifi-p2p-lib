package com.busylee.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.busylee.network.udp.UdpEngine;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;

/**
 * Created by busylee on 30.07.16.
 */
public class NetworkEngine extends Observable implements Network, Handler.Callback {

    private static final String TAG = "NetworkEngine";

    private static final int SEND_MESSAGE = 1;
    private static final int WAIT_NEXT_MESSAGE = 2;

    enum State {
        Idle,
        Running,
        Stopped,
    }

    private State mState = State.Idle;

    private final HandlerThread sendMessageThread;
    private final HandlerThread receiveThread;
    private Handler sendingHandler;
    private Handler receivingHandler;
    private Handler listenerHandler = new Handler(Looper.getMainLooper());

    private UdpEngine udpEngine;

    public NetworkEngine(UdpEngine udpEngine) {
        this(udpEngine, new HandlerThread("SendingThread", Process.THREAD_PRIORITY_BACKGROUND)
                , new HandlerThread("ReceivingThread", Process.THREAD_PRIORITY_BACKGROUND));
    }

    public NetworkEngine(UdpEngine udpEngine, HandlerThread sendMessageThread, HandlerThread receiveThread) {
        this.sendMessageThread = sendMessageThread;
        this.receiveThread = receiveThread;
        this.udpEngine = udpEngine;
    }

    public void start() {
        Log.d(TAG, "start()");
        if(mState == State.Running) {
            Log.w(TAG, "start() already starting");
            return;
        }

        mState = State.Running;

        if(!sendMessageThread.isAlive()) {
            sendMessageThread.start();
            sendingHandler = new Handler(sendMessageThread.getLooper(), this);
        }

        if(!receiveThread.isAlive()) {
            receiveThread.start();
            receivingHandler = new Handler(receiveThread.getLooper(), this);
            startWaiting();
        }
    }

    public void stop() {
        Log.d(TAG, "stop()");
        if(mState != State.Running) {
            Log.w(TAG, "start() not running");
            return;
        }

        if(sendMessageThread.isAlive() && !sendMessageThread.isInterrupted()) {
            sendMessageThread.quit();
            sendingHandler = null;
        }

        if(receiveThread.isAlive() && !receiveThread.isInterrupted()) {
            receiveThread.quit();
            receivingHandler = null;
        }

        mState = State.Stopped;

    }

    private void postToListener(final String message) {
        Log.d(TAG, "postToListener()");
        if(listenerHandler != null) {
            listenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    setChanged();
                    notifyObservers(message);
                }
            });
        }
    }

    private void startWaiting() {
        Log.d(TAG, "startWaiting()");
        receivingHandler.removeMessages(WAIT_NEXT_MESSAGE);
        Message waitMessage = new Message();
        waitMessage.what = WAIT_NEXT_MESSAGE;
        receivingHandler.sendMessage(waitMessage);
    }

    private void onWaitMessage() {
        Log.d(TAG, "onWaitMessage()");
        if(mState == State.Running) {
            try {
                String message = null;
                byte[] bytes = udpEngine.waitForNextMessage();
                if(bytes != null) {
                    message = new String(bytes, 0, bytes.length);
                }
                Log.d(TAG, "onWaitMessage() message = " + message);
                startWaiting();
                if(message != null) {
                    postToListener(message);
                } else {
                    Log.w(TAG, "onWaitMessage() message is null");
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                Log.d(TAG, "onWaitMessage() timeout waiting message");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.d(TAG, "onWaitMessage() error waiting message");
            }
        } else {
            Log.d(TAG, "onWaitMessage() state is not Running");
        }
    }

    protected final void onSendMessage(String message) {
        if(mState == State.Running) {
            try {
                udpEngine.sendMessage(message.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onSendMessage() error during send message");
            }
        } else {
            Log.d(TAG, "onSendMessage() state is not Running");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SEND_MESSAGE:
                onSendMessage((String) msg.obj);
                return true;
            case WAIT_NEXT_MESSAGE:
                onWaitMessage();
                return true;
        }
        return false;
    }

    @Override
    public void sendMessageBroadcast(String message) {
        sendBroadcastMessageDelayed(message, 0);
    }

    protected void sendBroadcastMessageDelayed(String message, int delay) {
        Message sendMessage = new Message();
        sendMessage.obj = message;
        sendMessage.what = SEND_MESSAGE;
        postToSendingThread(sendMessage, delay);
    }

    protected void postToSendingThread(Message sendMessage, int delay) {
        if(mState == State.Running) {
            sendingHandler.sendMessageDelayed(sendMessage, delay);
        } else {
            Log.e(TAG, "sendMessageBroadcast() state is not Running");
        }
    }

    @Override
    public InetAddress getIpAddress() {
        return udpEngine.getIpAddress();
    }


}
