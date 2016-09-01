package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.busylee.network.session.endpoint.Endpoint;
import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;
import com.google.gson.GsonBuilder;

import java.util.Observable;

import static com.busylee.network.message.Message.Command.*;

/**
 * Created by busylee on 03.08.16.
 */
public class UdpBroadcastSession extends AbstractSession implements NetworkEngine.NetworkListener{

    private static final String TAG = "UdpBroadcastSession";
    private final NetworkEngine networkEngine;
    private EndPointListener mEndpointListener;

    UdpBroadcastSession(NetworkEngine networkEngine) {
        this.networkEngine = networkEngine;
        this.networkEngine.addObserver(this);
    }

    @Override
    public void ping() {
        //TODO move here auth logic
    }

    @Override
    public void sendDataMessage(String messageBody) {
        //TODO think about necessary of setting address from here
        Message message = new Message.Builder()
                .setCommand(DATA)
                .setData(messageBody)
                .build();
        networkEngine.sendMessageBroadcast(message.toString().getBytes());
    }

    @Override
    public void setSessionListener(SessionListener sessionListener) {

    }

    @Override
    public void sendMessage(Message message) {
        networkEngine.sendMessageBroadcast(message.toString().getBytes());
    }

    public void setEndpointListener(EndPointListener endPointListener) {
        this.mEndpointListener = endPointListener;
    }

    @Override
    public EState getState() {
        return EState.Established;
    }

    @Override
    public void close() {
        //TODO
    }

    @Override
    public void update(Observable observable, Object data) {
        Message message = new GsonBuilder().create().fromJson((String) data, Message.class);
        if(message.getCommand() == null) {
            Log.w(TAG, "missing command in message = " + message);
            return;
        }
        switch (message.getCommand()) {
            case PEER:
                if(mEndpointListener != null) {
                    Endpoint endpoint = null;
                    if(message.getAddressFrom() != null) {
                        endpoint = new UserEndpoint(message.getId(), message.getAddressFrom());
                    } else if (message.getId() != null) {
                        endpoint = new GroupEndpoint(message.getId());
                    }

                    if(endpoint != null) {
                        mEndpointListener.onEndpointInfoReceived(endpoint);
                    }
                }
                break;
            case INVITE:
                if(mEndpointListener != null) {
                    mEndpointListener.onSessionInvitationReceived(
                            new UserEndpoint(message.getId(), message.getAddressFrom())
                    );
                }
                break;
        }
    }

    public interface EndPointListener {
        void onSessionInvitationReceived(UserEndpoint inetAddress);
        void onEndpointInfoReceived(Endpoint endpoint);
    }
}
