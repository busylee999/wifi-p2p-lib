package com.busylee.network.serialization;

import com.busylee.network.message.Message;

/**
 * Created by busylee on 01.09.16.
 */

public interface SerializationListener {
    void onMessage(Message message);
}
