package com.busylee.network.session.endpoint;

import android.text.TextUtils;

import java.net.InetAddress;

/**
 * Created by busylee on 04.08.16.
 */
public class UserEndpoint extends Endpoint {
    private final String id;
    private final InetAddress address;

    public UserEndpoint(String id, InetAddress address) {
        this.id = id;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "UserEndpoint{id=" + id +",address=" + address.getHostAddress() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UserEndpoint))
            return false;

        UserEndpoint userEndpoint = ((UserEndpoint) o);
        if(!TextUtils.equals(userEndpoint.id, id)
                || !userEndpoint.address.equals(address)) {
            return false;
        }

        return true;
    }
}
