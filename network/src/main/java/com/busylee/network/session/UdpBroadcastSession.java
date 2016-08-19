package com.busylee.network.session;

import android.util.Log;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
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
    public UdpBroadcastSession(NetworkEngine networkEngine) {
        this.networkEngine = networkEngine;
        this.networkEngine.addObserver(this);
    }

    @Override
    public void ping() {
        //TODO move here auth logic
    }

    @Override
    public void sendMessage(String message) {
        //TODO
    }

    @Override
    public void setSessionListener(SessionListener sessionListener) {

    }

    public void sendMessage(Message message) {
        networkEngine.sendMessageBroadcast(message.toString());
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
                    UserEndpoint userEndpoint =
                            new UserEndpoint(message.getId(), message.getAddressFrom());
                    mEndpointListener.onEndpointInfoReceived(userEndpoint);
                }
                break;
            case INVITE:
                if(mEndpointListener != null) {
                    mEndpointListener.onPotentialSessionReceived(
                            new UserEndpoint(message.getId(), message.getAddressFrom())
                    );
                }
                break;
        }
    }

    public interface EndPointListener {
        void onPotentialSessionReceived(UserEndpoint inetAddress);
        void onEndpointInfoReceived(UserEndpoint endpoint);
    }
}
