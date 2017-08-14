package org.zanata.notification;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import com.google.common.collect.Lists;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;

import static org.mockito.Mockito.*;

@RunWith(CdiUnitRunner.class)
@InRequestScope
@SupportDeltaspikeCore
public class EmailQueueMessageReceiverTest {
    @Inject
    private EmailQueueMessageReceiver receiver;
    @Produces @Mock
    private LanguageTeamPermissionChangeJmsMessagePayloadHandler languageTeamHandler;
    @Produces
    private IServiceLocator serviceLocator = ServiceLocator.instance();

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
