package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.NotificationEventHandler;
import org.zanata.webtrans.client.presenter.NotificationPresenter.Display;
import org.zanata.webtrans.client.presenter.NotificationPresenter.DisplayOrder;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

@Test(groups = { "unit-tests" })
public class NotificationPresenterTest extends PresenterTest
{
   private NotificationPresenter notificationPresenter;

   HasClickHandlers mockDismiss;
   HasClickHandlers mockClear;

   HasNotificationLabel mockListener;
   
   Display mockDisplay;
   EventBus mockEventBus;

   private Capture<ClickHandler> capturedDismissClickHandler;
   private Capture<ClickHandler> capturedClearClickHandler;
   private Capture<NotificationEventHandler> capturedNotificationEventHandler;
   
   private final static int MSG_TO_KEEP = 500;

   @BeforeClass
   public void createMocks()
   {
      mockDismiss = createAndAddMock(HasClickHandlers.class);
      mockClear = createAndAddMock(HasClickHandlers.class);
      mockDisplay = createAndAddMock(NotificationPresenter.Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockListener = createAndAddMock(HasNotificationLabel.class);

      capturedDismissClickHandler = addCapture(new Capture<ClickHandler>());
      capturedClearClickHandler = addCapture(new Capture<ClickHandler>());
      capturedNotificationEventHandler = addCapture(new Capture<NotificationEventHandler>());
   }

   @BeforeMethod
   void beforeMethod()
   {
      resetAll();
      notificationPresenter = new NotificationPresenter(mockDisplay, mockEventBus);
   }

   public void testOnBind()
   {
      mockListener.setNotificationLabel(0, Severity.Info);
      expectLastCall().once();
      
      replayAllMocks();
      notificationPresenter.bind();
      notificationPresenter.setNotificationListener(mockListener);
      verifyAllMocks();
   }

   public void testErrorNotificationShows()
   {
      String testMessage = "error testing";

      mockDisplay.appendMessage(Severity.Error, testMessage, null);
      expectLastCall().once();

      expect(mockDisplay.getMessageCount()).andReturn(1);
      
      mockListener.setNotificationLabel(0, Severity.Info);
      expectLastCall().once();
      
      mockListener.setNotificationLabel(1, Severity.Error);
      expectLastCall().once();
      
      replayAllMocks();
      notificationPresenter.bind();
      notificationPresenter.setNotificationListener(mockListener);
      NotificationEvent notification = new NotificationEvent(Severity.Error, testMessage);
      capturedNotificationEventHandler.getValue().onNotification(notification);

      verifyAllMocks();
   }

   public void testErrorMessageCount()
   {
      String[] testMessages = { "test1", "test2", "test3", "test4", "test5" };

      for (String msg : testMessages)
      {
         mockDisplay.appendMessage(Severity.Error, msg, null);
         expectLastCall().once();
      }

      for (int count = 0;count<testMessages.length;count++)
      {
         expect(mockDisplay.getMessageCount()).andReturn(count);

         mockListener.setNotificationLabel(count, Severity.Error);
         expectLastCall().once();
      }

      mockListener.setNotificationLabel(0, Severity.Info);
      expectLastCall().once();

      replayAllMocks();
      notificationPresenter.bind();
      notificationPresenter.setNotificationListener(mockListener);
      for (String msg : testMessages)
      {
         NotificationEvent notification = new NotificationEvent(Severity.Error, msg);
         capturedNotificationEventHandler.getValue().onNotification(notification);
      }

      verifyAllMocks();
   }

   public void testErrorMessageCountExceedMax()
   {
      String[] testMessages = { "test1", "test2", "test3", "test4", "test5", "test6", "test7" };

      for (String msg : testMessages)
      {
         mockDisplay.appendMessage(Severity.Error, msg, null);
         expectLastCall().once();
      }
      
      for (int count = 0;count<testMessages.length;count++)
      {
         expect(mockDisplay.getMessageCount()).andReturn(count);

         mockListener.setNotificationLabel(count, Severity.Error);
         expectLastCall().once();
      }
      
      mockListener.setNotificationLabel(0, Severity.Info);
      expectLastCall().once();
      
      replayAllMocks();
      notificationPresenter.bind();
      notificationPresenter.setNotificationListener(mockListener);
      
      for (String msg : testMessages)
      {
         NotificationEvent notification = new NotificationEvent(Severity.Error, msg);
         capturedNotificationEventHandler.getValue().onNotification(notification);
      }

      verifyAllMocks();
   }

   @Override
   protected void setDefaultBindExpectations()
   {
      expectHandlerRegistrations();
      expectPresenterSetupActions();
      setupMockGetterReturnValues();
   }

   private void expectHandlerRegistrations()
   {
      expectClickHandlerRegistration(mockDismiss, capturedDismissClickHandler);
      expectClickHandlerRegistration(mockClear, capturedClearClickHandler);
      expectEventHandlerRegistration(mockEventBus, NotificationEvent.getType(), NotificationEventHandler.class, capturedNotificationEventHandler);
   }

   private void expectPresenterSetupActions()
   {
      mockDisplay.setMessagesToKeep(MSG_TO_KEEP);
      mockDisplay.setMessageOrder(DisplayOrder.ASCENDING);
   }

   private void setupMockGetterReturnValues()
   {
      expect(mockDisplay.getClearButton()).andReturn(mockClear).anyTimes();
   }
}
