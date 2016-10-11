package com.busylee.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.busylee.network.module.HandlerThreadModule;
import com.busylee.network.udp.UdpEngine;
import com.busylee.network.utils.AndroidLogger;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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
    private final Logger logger;

    public NetworkEngine(UdpEngine udpEngine) {
        this(udpEngine, new AndroidLogger());
    }

    public NetworkEngine(UdpEngine udpEngine, Logger logger) {
        this(udpEngine, new HandlerThreadModule().provideSensingThread()
                , new HandlerThreadModule().provideReceivingThread(), logger);
    }

    @Inject @Singleton
    public NetworkEngine(UdpEngine udpEngine,
                         @Named("sending") HandlerThread sendMessageThread,
                         @Named("receiving") HandlerThread receiveThread, Logger logger) {
        this.sendMessageThread = sendMessageThread;
        this.receiveThread = receiveThread;
        this.udpEngine = udpEngine;
        this.logger = logger;
    }

    public void start() {
        logger.d(TAG, "start()");
        if(mState == State.Running) {
            logger.w(TAG, "start() already starting");
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
        logger.d(TAG, "stop()");
        if(mState != State.Running) {
            logger.w(TAG, "start() not running");
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

    private void postToListener(final byte[] message) {
        logger.d(TAG, "postToListener()");
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
        logger.d(TAG, "startWaiting()");
        receivingHandler.removeMessages(WAIT_NEXT_MESSAGE);
        Message waitMessage = new Message();
        waitMessage.what = WAIT_NEXT_MESSAGE;
        receivingHandler.sendMessage(waitMessage);
    }

    private void onWaitMessage() {
        logger.d(TAG, "onWaitMessage()");
        if(mState == State.Running) {
            try {
                byte[] bytes = udpEngine.waitForNextMessage();
                startWaiting();
                if(bytes != null) {
                    logger.w(TAG, "onWaitMessage() message is " + new String(bytes));
                    postToListener(bytes);
                } else {
                    logger.w(TAG, "onWaitMessage() message is null");
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                logger.d(TAG, "onWaitMessage() timeout waiting message");
            } catch (SocketException e) {
                e.printStackTrace();
                logger.d(TAG, "onWaitMessage() error waiting message");
            }
        } else {
            logger.d(TAG, "onWaitMessage() state is not Running");
        }
    }

    protected final void onSendMessage(byte[] message) {
        if(mState == State.Running) {
            try {
                udpEngine.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                logger.d(TAG, "onSendMessage() error during send message");
            }
        } else {
            logger.d(TAG, "onSendMessage() state is not Running");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SEND_MESSAGE:
                onSendMessage((byte[]) msg.obj);
                return true;
            case WAIT_NEXT_MESSAGE:
                onWaitMessage();
                return true;
        }
        return false;
    }

    @Override
    public void sendMessageBroadcast(byte[] message) {
        sendBroadcastMessageDelayed(message, 0);
    }

    protected void sendBroadcastMessageDelayed(byte[] message, int delay) {
        Message sendMessage = new Message();
        sendMessage.obj = message;
        sendMessage.what = SEND_MESSAGE;
        postToSendingThread(sendMessage, delay);
    }

    protected void postToSendingThread(Message sendMessage, int delay) {
        if(mState == State.Running) {
            sendingHandler.sendMessageDelayed(sendMessage, delay);
        } else {
            logger.e(TAG, "sendMessageBroadcast() state is not Running");
        }
    }

    @Override
    public InetAddress getIpAddress() {
        return udpEngine.getIpAddress();
    }


}
