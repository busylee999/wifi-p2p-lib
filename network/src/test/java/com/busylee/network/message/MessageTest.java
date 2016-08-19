package com.busylee.network.message;

import com.busylee.network.Assert;
import com.busylee.network.session.endpoint.UserEndpoint;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by busylee on 03.08.16.
 */
public class MessageTest {
    String addressString;
    String id;
    String data;
    InetAddress address;
    String expectedFullMessage;
    @Before
    public void setup() throws UnknownHostException {
        addressString = "1.1.1.1";
        id = "id";
        address = InetAddress.getByName(addressString);
        data = "testMessage";
    }

    @Test
    public void shouldConvertToStringCorrect() throws UnknownHostException {
        expectedFullMessage = "{" +
                "\"addressTo\":\"1.1.1.1\"," +
                "\"id\":\"id\"," +
                "\"data\":\"testMessage\"}";
        Message message = new Message.Builder()
                .setAddressTo(address)
                .setId(id)
                .setData(data)
                .build();

        Assert.assertEquals(expectedFullMessage, message.toString());
    }

    @Test
    public void shouldAddCommand() throws UnknownHostException {
        expectedFullMessage = "{" +
                "\"addressTo\":\"1.1.1.1\"," +
                "\"id\":\"id\"," +
                "\"command\":\"PEER\"," +
                "\"data\":\"testMessage\"}";
        Message message = new Message.Builder()
                .setAddressTo(address)
                .setId(id)
                .setData(data)
                .setCommand(Message.Command.PEER)
                .build();

        Assert.assertEquals(expectedFullMessage, message.toString());
    }

    @Test
    public void shouldAddUserInfoToMessage() throws UnknownHostException {
        expectedFullMessage = "{" +
                "\"addressTo\":\"1.1.1.1\"," +
                "\"id\":\"id\"}";
        UserEndpoint userEndpoint = new UserEndpoint(id, address);
        Message message = new Message.Builder()
                .setEndpoint(userEndpoint)
                .build();

        Assert.assertEquals(expectedFullMessage, message.toString());
    }
}
