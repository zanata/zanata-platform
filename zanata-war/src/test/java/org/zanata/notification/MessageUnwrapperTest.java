package org.zanata.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.zanata.util.EmptyEnumeration;

@Test(groups = "unit-tests")
public class MessageUnwrapperTest {

    @Test
    public void testUnwrapObjectMessage() throws Exception {
        ObjectMessage message = Mockito.mock(ObjectMessage.class);
        when(message.getObject()).thenReturn("payload");
        when(message.getPropertyNames())
                .thenReturn(new StringTokenizer("prop1 prop2"));
        when(message.getObjectProperty("prop1")).thenReturn("a");
        when(message.getObjectProperty("prop2")).thenReturn("b");

        MessageUnwrapper unwrap = MessageUnwrapper.unwrap(message);

        assertThat(unwrap.toString())
                .isEqualTo(
                        "MessageUnwrapper{payload=payload, properties={prop1=a, prop2=b}}");

    }

    @Test
    public void testUnwrapTextMessage() throws JMSException {
        TextMessage message = Mockito.mock(TextMessage.class);
        when(message.getText()).thenReturn("payload");
        when(message.getPropertyNames())
                .thenReturn(new StringTokenizer("prop1 prop2"));
        when(message.getObjectProperty("prop1")).thenReturn("a");
        when(message.getObjectProperty("prop2")).thenReturn("b");

        MessageUnwrapper unwrap = MessageUnwrapper.unwrap(message);

        assertThat(unwrap.toString())
                .isEqualTo(
                        "MessageUnwrapper{payload=payload, properties={prop1=a, prop2=b}}");
    }

    @Test
    public void testUnwrapGenericMessage() throws JMSException {
        Message message = Mockito.mock(Message.class);
        when(message.getPropertyNames()).thenReturn(new EmptyEnumeration());

        MessageUnwrapper unwrap = MessageUnwrapper.unwrap(message);

        assertThat(unwrap.toString()).contains("Mock for Message");
    }
}
