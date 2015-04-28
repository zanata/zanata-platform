package org.zanata.notification;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import com.google.common.collect.Lists;

import static org.mockito.Mockito.*;

@Test(groups = "unit-tests")
public class EmailQueueMessageReceiverTest {
    private EmailQueueMessageReceiver receiver;
    @Mock
    private LanguageTeamPermissionChangeJmsMessagePayloadHandler languageTeamHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        receiver = new EmailQueueMessageReceiver(languageTeamHandler);
    }

    @Test
    public void willSkipNonObjectJmsMessage() {
        for (Message message : Lists.newArrayList(
                mock(TextMessage.class),
                mock(Message.class),
                mock(BytesMessage.class),
                mock(MapMessage.class))) {
            receiver.onMessage(message);
        }
        verifyZeroInteractions(languageTeamHandler);
    }

    @Test
    public void willNotHandleIfCanNotFindHandlerForObjectType()
            throws JMSException {
        ObjectMessage message = mock(ObjectMessage.class);
        when(message.getStringProperty(NotificationManager.MessagePropertiesKey.objectType.name())).thenReturn("unknownType");
        receiver.onMessage(message);
        verifyZeroInteractions(languageTeamHandler);
    }

    @Test
    public void willHandleIfMessageIsCorrectType() throws JMSException {
        ObjectMessage message = mock(ObjectMessage.class);
        when(message.getStringProperty(
                NotificationManager.MessagePropertiesKey.objectType.name()))
                .thenReturn(LanguageTeamPermissionChangedEvent.class.getCanonicalName());
        when(message.getObject()).thenReturn("payload");
        receiver.onMessage(message);
        verify(languageTeamHandler).handle("payload");
    }
}
