package org.zanata.webtrans.client;

import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.TargetContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.client.ui.DialogBoxCloseButton;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.RemoteLoggingAction;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
class UncaughtExceptionHandlerImpl implements GWT.UncaughtExceptionHandler
{

   private final DialogBox globalPopup = new DialogBox(false, true);
   private final DialogBoxCloseButton closeButton = new DialogBoxCloseButton(globalPopup);

   private final CachingDispatchAsync dispatcher;
   private final UserConfigHolder configHolder;
   private AppPresenter appPresenter;
   private TargetContentsPresenter targetContentsPresenter;

   protected UncaughtExceptionHandlerImpl(CachingDispatchAsync dispatcher, UserConfigHolder configHolder)
   {
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
      globalPopup.setGlassEnabled(true);
   }

   @Override
   public void onUncaughtException(Throwable exception)
   {
      Throwable e = unwrapUmbrellaException(exception);
      Log.fatal("uncaught exception", e);

      String stackTrace = buildStackTraceMessages(e);
/*    disable server side logging for now to avoid email bombing

      RemoteLoggingAction action = new RemoteLoggingAction(stackTrace);
      action.addContextInfo("selected Doc", appPresenter.getSelectedDocumentInfoOrNull());
      action.addContextInfo("selected TransUnitId", targetContentsPresenter.getCurrentTransUnitIdOrNull());
      action.addContextInfo("editor contents", targetContentsPresenter.getNewTargets());
      dispatcher.execute(action, new NoOpAsyncCallback<NoOpResult>());
*/

      if (!configHolder.getState().isShowError())
      {
         return;
      }
      globalPopup.getCaption().setHTML("<div class=\"globalPopupCaption\">ERROR: " + e.getMessage() + "</div>");

      VerticalPanel popupContent = new VerticalPanel();

      // description text
      SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
      // @formatter:off
      htmlBuilder
            .appendHtmlConstant("<h3>You may close this window and continue with your work</h3>")
            .appendHtmlConstant("<div>If you want to let us know the error, Please recall your actions and take one of the following steps:</div>")
            .appendHtmlConstant("<ul>")
            .appendHtmlConstant("<li>Email administration; Or</li>")
            .appendHtmlConstant("<li>Check if it's a <a href=\"https://bugzilla.redhat.com/buglist.cgi?product=Zanata&bug_status=__open__\" target=\"_blank\">known issue</a>; Or</li>")
            .appendHtmlConstant("<li><a href=\"https://bugzilla.redhat.com/enter_bug.cgi?format=guided&product=Zanata\" target=\"_blank\">Report a problem</a>; Or</li>")
            .appendHtmlConstant("<li>Email <a href=\"mailto:zanata-users@redhat.com\">Zanata users mailing list</a></li>")
            .appendHtmlConstant("</ul>")
      // @formatter:on
      ;

      // stack trace
      DisclosurePanel disclosurePanel = buildStackTraceDisclosurePanel(stackTrace);

      // send stack trace log to server

      popupContent.add(new HTMLPanel(htmlBuilder.toSafeHtml()));
      popupContent.add(disclosurePanel);
      popupContent.add(closeButton);
      globalPopup.setWidget(popupContent);
      globalPopup.center();
   }

   public static Throwable unwrapUmbrellaException(Throwable e)
   {
      if(e instanceof UmbrellaException)
      {
         UmbrellaException ue = (UmbrellaException) e;
         if(ue.getCauses().size() == 1)
         {
            return unwrapUmbrellaException(ue.getCauses().iterator().next());
         }
      }
      return e;
   }

   protected static String buildStackTraceMessages(Throwable e)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(e.getMessage()).append("\n");
      for (StackTraceElement ste : e.getStackTrace())
      {
         builder.append("\tat ").append(ste.toString()).append("\n");
      }
      return builder.toString();
   }

   private static DisclosurePanel buildStackTraceDisclosurePanel(String stackTrace)
   {
      DisclosurePanel disclosurePanel = new DisclosurePanel("Stack trace of the exception (helpful to us)");
      disclosurePanel.getHeader().getParent().setStyleName(""); //conflict style from menu.css
      SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
      htmlBuilder.appendHtmlConstant("<pre>");
      htmlBuilder.appendHtmlConstant(stackTrace);
      htmlBuilder.appendHtmlConstant("</pre>");
      disclosurePanel.setContent(new HTMLPanel(htmlBuilder.toSafeHtml()));
      disclosurePanel.setOpen(false);
      return disclosurePanel;
   }

   public void setAppPresenter(AppPresenter appPresenter)
   {
      this.appPresenter = appPresenter;
   }

   public void setTargetContentsPresenter(TargetContentsPresenter targetContentsPresenter)
   {
      this.targetContentsPresenter = targetContentsPresenter;
   }
}
