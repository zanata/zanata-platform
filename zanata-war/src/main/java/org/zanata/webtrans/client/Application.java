package org.zanata.webtrans.client;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.EventProcessor.StartCallback;
import org.zanata.webtrans.client.gin.WebTransGinjector;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceResult;
import org.zanata.webtrans.shared.rpc.NoOpResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint
{

   private static final String APP_LOAD_ERROR_CSS_CLASS = "AppLoadError";

   private static boolean IS_DEBUG = false;
   private static WorkspaceId workspaceId;
   private static UserWorkspaceContext userWorkspaceContext;

   private static Identity identity;

   private final static WebTransGinjector injector = GWT.create(WebTransGinjector.class);

   private final DialogBox globalPopup = new DialogBox(false, true);
   private final Button closeGlobalPopupButton = new Button("Close", new ClickHandler()
   {
      @Override
      public void onClick(ClickEvent event)
      {
         globalPopup.hide();
      }
   });

   public void onModuleLoad()
   {
      globalPopup.setGlassEnabled(true);
      registerUncaughtExceptionHandler();

      injector.getDispatcher().execute(new ActivateWorkspaceAction(getWorkspaceId()), new AsyncCallback<ActivateWorkspaceResult>()
      {

         @Override
         public void onFailure(Throwable caught)
         {
            try
            {
               throw caught;
            }
            catch (AuthenticationError e)
            {
               redirectToLogin();
            }
            catch (NoSuchWorkspaceException e)
            {
               Log.error("Invalid workspace", e);
               String errorMessage, linkText, projectListUrl;
               errorMessage = "Invalid Workspace. Try opening the workspace from the link on the project page.";
               linkText = "Projects";
               projectListUrl = getModuleParentBaseUrl() + "project/list";
               showErrorWithLink(errorMessage, null, linkText, projectListUrl);
            }
            catch (Throwable e)
            {
               Log.error("An unexpected Error occurred", e);
               showErrorWithLink("An unexpected Error occurred: " + e.getMessage(), e, null, null);
            }
         }

         @Override
         public void onSuccess(ActivateWorkspaceResult result)
         {
            userWorkspaceContext = result.getUserWorkspaceContext();
            identity = result.getIdentity();
            injector.getDispatcher().setIdentity(identity);
            injector.getDispatcher().setUserWorkspaceContext(userWorkspaceContext);
            startApp();
         }

      });

   }

   public static void exitWorkspace()
   {
      injector.getDispatcher().execute(new ExitWorkspaceAction(identity.getPerson()), new NoOpAsyncCallback<ExitWorkspaceResult>());
   }

   private void startApp()
   {
      // When user close the workspace, send ExitWorkSpaceAction
      Window.addWindowClosingHandler(new Window.ClosingHandler()
      {
         @Override
         public void onWindowClosing(ClosingEvent event)
         {
            exitWorkspace();
         }
      });

      final EventProcessor eventProcessor = injector.getEventProcessor();
      eventProcessor.start(new StartCallback()
      {
         @Override
         public void onSuccess(String connectionId)
         {
            // tell server the ConnectionId for this EditorClientId
            injector.getDispatcher().execute(new EventServiceConnectedAction(connectionId), new AsyncCallback<NoOpResult>()
            {
               @Override
               public void onFailure(Throwable e)
               {
                  RootPanel.get("contentDiv").add(new HTML("<h1>Server communication failed...</h1>" + "<b>Exception:</b> " + e.getMessage()));
               }
               @Override
               public void onSuccess(NoOpResult result)
               {
                  delayedStartApp();
               }
            });
         }

         @Override
         public void onFailure(Throwable e)
         {
            RootPanel.get("contentDiv").add(new HTML("<h1>Failed to start Event Service...</h1>" + "<b>Exception:</b> " + e.getMessage()));
         }
      });

      Window.enableScrolling(true);
   }

   private void delayedStartApp()
   {
      final AppPresenter appPresenter = injector.getAppPresenter();
      RootPanel.get("contentDiv").add(appPresenter.getDisplay().asWidget());
      appPresenter.bind();
      Window.enableScrolling(true);
   }

   public static ProjectIterationId getProjectIterationId()
   {
      String projectSlug = Window.Location.getParameter("project");
      String iterationSlug = Window.Location.getParameter("iteration");
      if (projectSlug == null || iterationSlug == null)
      {
         return null;
      }
      try
      {
         return new ProjectIterationId(projectSlug, iterationSlug);
      }
      catch (NumberFormatException nfe)
      {
         return null;
      }
   }

   public static LocaleId getLocaleId()
   {
      String localeId = Window.Location.getParameter("localeId");
      return localeId == null ? null : new LocaleId(localeId);
   }

   public static void redirectToLogin()
   {
      redirectToUrl(getModuleParentBaseUrl() + "account/sign_in?continue=" + URL.encodeQueryString(Window.Location.getHref()));
   }

   public static void redirectToLogout()
   {
      redirectToUrl(getModuleParentBaseUrl() + "account/sign_out");
   }

   public static void redirectToZanataProjectHome(WorkspaceId workspaceId)
   {
      redirectToUrl(getModuleParentBaseUrl() + "project/view/" + workspaceId.getProjectIterationId().getProjectSlug());
   }

   public static void redirectToIterationFiles(WorkspaceId workspaceId)
   {
      redirectToUrl(getModuleParentBaseUrl() + "iteration/files/" + workspaceId.getProjectIterationId().getProjectSlug() + "/" + workspaceId.getProjectIterationId().getIterationSlug() + "/" + workspaceId.getLocaleId().getId());
   }

   public static native void redirectToUrl(String url)/*-{
		$wnd.location = url;
   }-*/;

   public static WorkspaceId getWorkspaceId()
   {
      if (workspaceId == null)
      {
         workspaceId = new WorkspaceId(getProjectIterationId(), getLocaleId());
      }
      return workspaceId;
   }

   public static UserWorkspaceContext getUserWorkspaceContext()
   {
      return userWorkspaceContext;
   }

   public static Identity getIdentity()
   {
      return identity;
   }
  
   public static String getModuleParentBaseUrl()
   {
      return GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "");
   }

   /**
    * Display an error message instead of the web app. Shows a link if both text
    * and url are provided. Provides a stack trace if a {@link Throwable} is
    * provided.
    *
    * @param message to display
    * @param e non-null to provide a stack trace in an expandable view.
    * @param linkText text to display for link
    * @param linkUrl href for link
    */
   private static void showErrorWithLink(String message, Throwable e, String linkText, String linkUrl)
   {
      Label messageLabel = new Label(message);
      messageLabel.getElement().addClassName(APP_LOAD_ERROR_CSS_CLASS);
      FlowPanel layoutPanel = new FlowPanel();
      layoutPanel.add(messageLabel);

      if (Strings.isNullOrEmpty(linkText) && Strings.isNullOrEmpty(linkUrl))
      {
         Anchor a = new Anchor(linkText, linkUrl);
         a.getElement().addClassName(APP_LOAD_ERROR_CSS_CLASS);
         layoutPanel.add(a);
      }

      if (IS_DEBUG && e != null)
      {
         String stackTrace = "Stack trace for the error:<br/>\n";
         for (StackTraceElement ste : e.getStackTrace())
         {
            stackTrace += ste.toString() + "<br/>\n";
         }
         Label stackTraceLabel = new Label();
         stackTraceLabel.getElement().setInnerHTML(stackTrace);

         DisclosurePanel stackTracePanel = new DisclosurePanel("More detail:");
         stackTracePanel.getElement().addClassName(APP_LOAD_ERROR_CSS_CLASS);
         stackTracePanel.add(stackTraceLabel);
         layoutPanel.add(stackTracePanel);
      }

      RootPanel.get("contentDiv").add(layoutPanel);
   }

   private void registerUncaughtExceptionHandler()
   {
      GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler()
      {
         @Override
         public void onUncaughtException(Throwable e)
         {
            Log.error("uncaught exception", e);
            globalPopup.getCaption().setHTML("<div class=\"globalPopupCaption\">ERROR: " + e.getMessage() + "</div>");
            SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
            htmlBuilder.appendHtmlConstant("<h3>")
                  .appendEscaped("Stack trace for the error:")
                  .appendHtmlConstant("</h3>");
            htmlBuilder.appendHtmlConstant("<pre>");
            for (StackTraceElement ste : e.getStackTrace())
            {
               htmlBuilder.appendEscaped(ste.toString() + "\n\t");
            }
            htmlBuilder.appendHtmlConstant("</pre>");
            htmlBuilder.appendHtmlConstant("<h3>Please recall your actions and take one of the following steps:</h3>")
                  .appendHtmlConstant("<ul>")
                  .appendHtmlConstant("<li>Check if it's a <a href=\"https://bugzilla.redhat.com/buglist.cgi?product=Zanata&bug_status=__open__\" target=\"_blank\">known issue</a>; Or</li>")
                  .appendHtmlConstant("<li><a href=\"https://bugzilla.redhat.com/enter_bug.cgi?format=guided&product=Zanata\" target=\"_blank\">Report a problem</a>; Or</li>")
                  .appendHtmlConstant("<li>Email <a href=\"mailto:zanata-users@redhat.com\">Zanata users mailing list</a></li>")
                  .appendHtmlConstant("</ul>");
            HTMLPanel popupContent = new HTMLPanel(htmlBuilder.toSafeHtml());
            popupContent.add(closeGlobalPopupButton);
            globalPopup.setWidget(popupContent);
            globalPopup.center();
         }
      });
   }

}
