package com.busylee.network.serialization;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by busylee on 01.09.16.
 */
@RunWith(RobolectricTestRunner.class)
public class ProtoBufContextTest {

    @Test
    public void canCreate() {
        new ProtoBufContext();
    }

    @Test
    public void shouldSerializeAndDeserializeMessage() {

    }

}
