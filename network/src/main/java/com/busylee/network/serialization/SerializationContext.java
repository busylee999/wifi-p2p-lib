package com.busylee.network.serialization;

import com.busylee.network.Network;
import com.busylee.network.message.Message;

/**
 * Created by busylee on 01.09.16.
 */

public interface SerializationContext extends Network.NetworkListener {
    void sendMessage(Network network, Message message);
    void setListener(SerializationListener serializationListener);
}
