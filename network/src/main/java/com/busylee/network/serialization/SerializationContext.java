package com.busylee.network.serialization;

import com.busylee.network.Network;
import com.busylee.network.message.Message;

/**
 * Created by busylee on 01.09.16.
 */

public interface SerializationContext {
    byte[] serialize(Message message);
    Message deserialize(byte[] bytes);
}
