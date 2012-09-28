package org.zanata.webtrans.client.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.NotificationPresenter.DisplayOrder;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.view.NotificationDisplay;

@Test(groups = { "unit-tests" })
public class NotificationPresenterTest
{
   private NotificationPresenter notificationPresenter;

   @Mock
   private NotificationLabelListener mockListener;
   @Mock
   private NotificationDisplay mockDisplay;
   @Mock
   private EventBus mockEventBus;

   private final static int MSG_TO_KEEP = 100;

   @BeforeMethod
   void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      notificationPresenter = new NotificationPresenter(mockDisplay, mockEventBus);
   }

   @Test
   public void onBind()
   {
      notificationPresenter.bind();
      
      verify(mockDisplay).setMessagesToKeep(MSG_TO_KEEP);
      verify(mockDisplay).setMessageOrder(DisplayOrder.ASCENDING);
      verify(mockDisplay).setListener(notificationPresenter);
   }

   @Test
   public void onClearClick()
   {
      int msgCount = 5;
      
      when(mockDisplay.getMessageCount()).thenReturn(msgCount);
      
      notificationPresenter.setNotificationListener(mockListener);
      notificationPresenter.onClearClick();

      verify(mockDisplay).clearMessages();
      verify(mockListener).setNotificationLabel(msgCount, Severity.Info);
   }

   @Test
   public void onNotificationInfo()
   {
      int msgCount = 5;
      String msg = "Test message";
      Severity severity = Severity.Info;
      

      NotificationEvent mockEvent = mock(NotificationEvent.class);
      InlineLink mockInlineLink = mock(InlineLink.class);

      when(mockDisplay.getMessageCount()).thenReturn(msgCount);
      when(mockEvent.getSeverity()).thenReturn(severity);
      when(mockEvent.getMessage()).thenReturn(msg);
      when(mockEvent.getInlineLink()).thenReturn(mockInlineLink);

      notificationPresenter.bind();
      notificationPresenter.setNotificationListener(mockListener);
      notificationPresenter.onNotification(mockEvent);
      
      verify(mockListener).setNotificationLabel(msgCount, severity);
      verify(mockDisplay).appendMessage(severity, msg, mockInlineLink);
   }

   @Test
   public void onNotificationError()
   {
      int msgCount = 5;
      String msg = "Test message";
      Severity severity = Severity.Error;

      NotificationEvent mockEvent = mock(NotificationEvent.class);
      InlineLink mockInlineLink = mock(InlineLink.class);

      when(mockDisplay.getMessageCount()).thenReturn(msgCount);
      when(mockEvent.getSeverity()).thenReturn(severity);
      when(mockEvent.getMessage()).thenReturn(msg);
      when(mockEvent.getInlineLink()).thenReturn(mockInlineLink);

      notificationPresenter.setNotificationListener(mockListener);
      notificationPresenter.onNotification(mockEvent);

      verify(mockDisplay).appendMessage(severity, msg, mockInlineLink);
      verify(mockListener).showNotification();
      verify(mockListener).setNotificationLabel(msgCount, severity);
   }

   @Test
   public void onMsgCount()
   {
      int msgCount = 200;
      String msg = "Test message";
      Severity severity = Severity.Info;

      NotificationEvent mockEvent = mock(NotificationEvent.class);
      InlineLink mockInlineLink = mock(InlineLink.class);

      // when(mockDisplay.getMessageCount()).thenReturn(msgCount);
      when(mockEvent.getSeverity()).thenReturn(severity);
      when(mockEvent.getMessage()).thenReturn(msg);
      when(mockEvent.getInlineLink()).thenReturn(mockInlineLink);

      notificationPresenter.setNotificationListener(mockListener);
      for (int i = 0; i < msgCount; i++)
      {
         notificationPresenter.onNotification(mockEvent);
      }

      verify(mockDisplay, times(msgCount)).appendMessage(severity, msg, mockInlineLink);
   }
}
