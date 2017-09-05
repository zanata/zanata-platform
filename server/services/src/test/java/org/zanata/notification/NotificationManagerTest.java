package org.zanata.notification;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationManagerTest {
    private NotificationManager manager;
    @Mock
    private QueueSender mailQueueSender;
    @Mock
    private QueueSession queueSession;
    @Mock
    private ObjectMessage message;
    @Mock
    private QueueConnection connection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JmsResourcesProducer producer = new JmsResourcesProducer();
        manager = new NotificationManager();
    }

    @Test
    public void onLanguageTeamChangeEvent() throws JMSException {
        LanguageTeamPermissionChangedEvent event =
                Mockito.mock(LanguageTeamPermissionChangedEvent.class);
        when(queueSession.createObjectMessage(event)).thenReturn(message);

        manager.onLanguageTeamPermissionChanged(event, queueSession, mailQueueSender);

        verify(message).setObjectProperty(
                NotificationManager.MessagePropertiesKey.objectType.name(),
                event.getClass().getCanonicalName());
        verify(mailQueueSender).send(message);
    }
}
