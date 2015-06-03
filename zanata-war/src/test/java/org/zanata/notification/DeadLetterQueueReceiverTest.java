package org.zanata.notification;

import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.util.EmptyEnumeration;

import static org.mockito.Mockito.when;

public class DeadLetterQueueReceiverTest {
    private DeadLetterQueueReceiver receiver;
    @Mock
    private Message message;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        receiver = new DeadLetterQueueReceiver();
    }

    @Test
    public void testOnMessage() throws Exception {
        when(message.getPropertyNames())
                .thenReturn(new EmptyEnumeration<String>());
        receiver.onMessage(message);
        // placeholder for actual dead letter queue handling test
    }
}
