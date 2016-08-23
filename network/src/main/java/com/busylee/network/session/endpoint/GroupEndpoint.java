package com.busylee.network.session.endpoint;

import android.text.TextUtils;

/**
 * Created by busylee on 19.08.16.
 */
public class GroupEndpoint extends Endpoint {
    private final String id;

    public GroupEndpoint(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserEndpoint{id=" + id +"}";
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof GroupEndpoint))
            return false;

        GroupEndpoint groupEndpoint = ((GroupEndpoint) o);
        if(!TextUtils.equals(groupEndpoint.id, id)) {
            return false;
        }

        return true;
    }
}
