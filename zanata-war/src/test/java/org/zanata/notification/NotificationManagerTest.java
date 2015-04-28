package org.zanata.notification;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@Test(groups = "unit-tests")
public class NotificationManagerTest {
    private NotificationManager manager;
    @Mock
    private QueueSender mailQueueSender;
    @Mock
    private QueueSession queueSession;
    @Mock
    private ObjectMessage message;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        manager = new NotificationManager(mailQueueSender, queueSession);
    }

    @Test
    public void onLanguageTeamChangeEvent() throws JMSException {
        LanguageTeamPermissionChangedEvent event =
                Mockito.mock(LanguageTeamPermissionChangedEvent.class);
        when(queueSession.createObjectMessage(event)).thenReturn(message);

        manager.onLanguageTeamPermissionChanged(event);

        verify(message).setObjectProperty(
                NotificationManager.MessagePropertiesKey.objectType.name(),
                event.getClass().getCanonicalName());
        verify(mailQueueSender).send(message);
    }
}
