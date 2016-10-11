package com.busylee.network.message;

import com.busylee.network.session.endpoint.GroupEndpoint;
import com.busylee.network.session.endpoint.UserEndpoint;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by busylee on 03.08.16.
 */
public class Message {

    public static class Command {
        public static final String PING = "PING";
        public static final String INVITE = "INVITE";
        public static final String DATA = "DATA";
    }

    InetAddress addressFrom;
    InetAddress addressTo;
    String id;
    String roomId;
    String data;
    String command;

    public InetAddress getAddressFrom() {
        return addressFrom;
    }

    public InetAddress getAddressTo() {
        return addressTo;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getCommand() {
        return command;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        if(addressFrom != null) {
            appendInQuotes(stringBuilder, "addressFrom")
                    .append(":");
            appendInQuotes(stringBuilder, addressFrom.getHostAddress());
        }
        if(addressTo != null) {
            if(stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            appendInQuotes(stringBuilder, "addressTo")
                    .append(":");
            appendInQuotes(stringBuilder, addressTo.getHostAddress());
        }

        if(id != null) {
            if(stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            appendInQuotes(stringBuilder, "id")
                    .append(":");
            appendInQuotes(stringBuilder, id);
        }

        if(roomId != null) {
            if(stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            appendInQuotes(stringBuilder, "roomId")
                    .append(":");
            appendInQuotes(stringBuilder, roomId);
        }

        if(command != null) {
            if(stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            appendInQuotes(stringBuilder, "command")
                    .append(":");
            appendInQuotes(stringBuilder, command);
        }

        if(data != null) {
            if(stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            appendInQuotes(stringBuilder, "data")
                    .append(":");
            appendInQuotes(stringBuilder, data);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static StringBuilder appendInQuotes(StringBuilder stringBuilder, String string) {
        return stringBuilder.append("\"")
                .append(string)
                .append("\"");
    }

    public static class Builder {

        private InetAddress addressFrom;
        private InetAddress addressTo;
        private String id;
        private String data;
        private String command;

        public Builder setEndpoint(GroupEndpoint endpoint) {
            setId(endpoint.getId());
            return this;
        }

        public Builder setEndpoint(UserEndpoint endpoint) {
            setId(endpoint.getId());
            setAddressTo(endpoint.getAddress());
            return this;
        }

        public Builder setAddressFrom(InetAddress addressFrom) {
            this.addressFrom = addressFrom;
            return this;
        }

        public Builder setAddressTo(InetAddress addressTo) {
            this.addressTo = addressTo;
            return this;
        }

        public Builder setAddressTo(String address) throws UnknownHostException {
            this.addressTo = InetAddress.getByName(address);
            return this;
        }

        public Builder setAddressFrom(String address) throws UnknownHostException {
            this.addressFrom = InetAddress.getByName(address);
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setCommand(String command) {
            this.command = command;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.data = this.data;
            message.id = this.id;
            message.addressTo = this.addressTo;
            message.addressFrom = this.addressFrom;
            message.command = this.command;
            return message;
        }

        public static Builder from(Message message) {
            return new Builder()
                    .setData(message.data)
                    .setId(message.id)
                    .setAddressFrom(message.addressFrom)
                    .setAddressTo(message.addressTo)
                    .setCommand(message.command);
        }
    }
}
