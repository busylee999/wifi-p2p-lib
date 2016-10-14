package com.busylee.network.serialization;

import android.util.Base64;

import com.busylee.network.Assert;
import com.busylee.network.NetworkEngine;
import com.busylee.network.message.Message;
import com.google.gson.GsonBuilder;

import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.busylee.network.Assert.*;
import static org.mockito.Mockito.mock;

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
    public void shouldDeserialize() {
        String data = "test";
        Message actualMessage = base64Context.deserialize(getDataMessageBytes(data));
        final Message expectedMessage = getDataMessage(data);
        assertEquals(expectedMessage.toString(), actualMessage.toString());
    }

    @Test
    public void shouldSerialize() {
        String data = "test";
        byte[] actualMessage = base64Context.serialize(getDataMessage(data));
        Assert.assertThat(getDataMessageBytes(data), IsEqual.equalTo(actualMessage));
    }

    @Test
    public void canProcessMissedDataIncomingMessage() {
        Message actualMessage = base64Context.deserialize(getDataMessageBytes(null));
        final Message expectedMessage = getDataMessage(null);
        assertEquals(expectedMessage.toString(), actualMessage.toString());
    }

    @Test
    public void canProcessMissedDataOutgoingMessage() {
        byte[] actualMessage = base64Context.serialize(getDataMessage(null));
        byte[] expectedMessage = getDataMessageBytes(null);
        Assert.assertThat(getDataMessageBytes(null), IsEqual.equalTo(actualMessage));
    }

}
