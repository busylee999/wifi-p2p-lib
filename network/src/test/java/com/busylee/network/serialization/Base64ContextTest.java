package com.busylee.network.serialization;

import android.util.Base64;

import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by busylee on 01.09.16.
 */
@RunWith(RobolectricTestRunner.class)
public class Base64ContextTest {

    private Base64Context base64Context;
    private NetworkEngine networkEngineMock;

    @Before
    public void setUp() throws Exception {
        networkEngineMock = mock(NetworkEngine.class);
        base64Context = new Base64Context(new GsonBuilder().create());
    }

    private byte[] getDataMessageBytes(String data) {
        if(data != null) {
            String dataStringInBase64 = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
            return ("{\"data\":\"" + dataStringInBase64 + "\"}").getBytes();
        } else {
            return "{}".getBytes();
        }

    }

    private Message getDataMessage(String data) {
        return new Message.Builder()
                .setData(data)
                .build();
    }

    @Test
    public void shouldCallNetwork() {
        String data = "test";
        Message message = getDataMessage(data);
        base64Context.sendMessage(networkEngineMock, message);
        byte[] expectedBytes = getDataMessageBytes(data);

        verify(networkEngineMock).sendMessageBroadcast(expectedBytes);
    }

    @Test
    public void shouldCallSerializationListener() {
        String data = "test";
        SerializationListener serializationListener = mock(SerializationListener.class);
        base64Context.setListener(serializationListener);
        base64Context.update(null, getDataMessageBytes(data));
        final Message expectedMessage = getDataMessage(data);
        verify(serializationListener).onMessage(argThat(new ArgumentMatcher<Message>() {
            @Override
            public boolean matches(Object argument) {
                Message actualMessage = (Message) argument;
                return expectedMessage.toString().equals(actualMessage.toString());
            }
        }));
    }

    @Test
    public void canProcessMissedDataIncomingMessage() {
        SerializationListener serializationListener = mock(SerializationListener.class);
        base64Context.setListener(serializationListener);
        base64Context.update(null, getDataMessageBytes(null));
        final Message expectedMessage = getDataMessage(null);
        verify(serializationListener).onMessage(argThat(new ArgumentMatcher<Message>() {
            @Override
            public boolean matches(Object argument) {
                Message actualMessage = (Message) argument;
                return expectedMessage.toString().equals(actualMessage.toString());
            }
        }));
    }

    @Test
    public void canProcessMissedDataOutgoingMessage() {
        String data = null;
        Message message = getDataMessage(data);
        base64Context.sendMessage(networkEngineMock, message);
        byte[] expectedBytes = getDataMessageBytes(data);

        verify(networkEngineMock).sendMessageBroadcast(expectedBytes);
    }

}
