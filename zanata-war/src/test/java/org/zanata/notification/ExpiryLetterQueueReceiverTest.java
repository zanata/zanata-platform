package org.zanata.notification;

import javax.jms.Message;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.util.EmptyEnumeration;

import static org.mockito.Mockito.when;

@Test(groups = "unit-tests")
public class ExpiryLetterQueueReceiverTest {
    private ExpiryQueueReceiver receiver;
    @Mock
    private Message message;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        receiver = new ExpiryQueueReceiver();
    }

    @Test
    public void testOnMessage() throws Exception {
        when(message.getPropertyNames())
                .thenReturn(new EmptyEnumeration<String>());
        receiver.onMessage(message);
        // placeholder for actual expiry queue handling test
    }
}
