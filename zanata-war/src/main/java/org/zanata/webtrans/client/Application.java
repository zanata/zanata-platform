package org.zanata.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.EventProcessor.StartCallback;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.gin.WebTransGinjector;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;
import org.zanata.webtrans.shared.rpc.NoOpResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
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
   private UncaughtExceptionHandlerImpl exceptionHandler;

   public void onModuleLoad()
   {
      exceptionHandler = new UncaughtExceptionHandlerImpl(injector.getDispatcher(), injector.getUserConfig());
      GWT.setUncaughtExceptionHandler(exceptionHandler);

      injector.getDispatcher().execute(new ActivateWorkspaceAction(getWorkspaceId()), new AsyncCallback<ActivateWorkspaceResult>()
      {

         @Override
         public void onFailure(Throwable caught)
         {
            if (caught instanceof AuthenticationError)
            {
               redirectToLogin();
            }
            else if (caught instanceof NoSuchWorkspaceException)
            {
               Log.error("Invalid workspace", caught);
               String errorMessage;
               errorMessage = "Invalid Workspace. " + caught.getLocalizedMessage() + ". Try opening the workspace from the link on the project page.";
               showErrorWithLink(errorMessage, caught);
            }
            else
            {
               Log.error("An unexpected Error occurred", caught);
               showErrorWithLink("An unexpected Error occurred: " + caught.getMessage(), caught);
            }
         }

         @Override
         public void onSuccess(final ActivateWorkspaceResult result)
         {
            userWorkspaceContext = result.getUserWorkspaceContext();
            identity = result.getIdentity();
            injector.getDispatcher().setIdentity(identity);
            injector.getDispatcher().setUserWorkspaceContext(userWorkspaceContext);
            injector.getDispatcher().setEventBus(injector.getEventBus());
            injector.getUserConfig().setState( result.getStoredUserConfiguration() );

            startApp();
         }

      });

   }

   public static void exitWorkspace()
   {
      injector.getDispatcher().execute(new ExitWorkspaceAction(identity.getPerson()), new NoOpAsyncCallback<NoOpResult>());
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
                  showErrorWithLink("Server communication failed...", e);
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
            showErrorWithLink("Failed to start Event Service...", e);
         }
      });

      Window.enableScrolling(true);
   }

   private void delayedStartApp()
   {
      final AppPresenter appPresenter = injector.getAppPresenter();
      final DocumentListPresenter documentListPresenter = injector.getDocumentListPresenter();
      RootPanel.get("contentDiv").add(appPresenter.getDisplay().asWidget());
      appPresenter.bind();
      Window.enableScrolling(true);
      // eager load document list
      final EventBus eventBus = injector.getEventBus();
      injector.getDispatcher().execute(new GetDocumentList(getWorkspaceId().getProjectIterationId(), injector.getLocation().getQueryDocuments()), new AsyncCallback<GetDocumentListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, "Failed to load documents"));
         }

         @Override
         public void onSuccess(GetDocumentListResult result)
         {
            long start = System.currentTimeMillis();
            final ArrayList<DocumentInfo> documents = result.getDocuments();
            Log.info("Received doc list for " + result.getProjectIterationId() + ": " + documents.size() + " elements, loading time: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
            documentListPresenter.setDocuments(documents);

            History history = injector.getHistory();
            history.addValueChangeHandler(injector.getHistoryEventHandlerService());
            Log.info("=========== now firing current history state =========== ");
            history.fireCurrentHistoryState();

            start = System.currentTimeMillis();
            TranslationStats projectStats = new TranslationStats(); // = 0
            for (DocumentInfo doc : documents)
            {
               projectStats.add(doc.getStats());
            }

            documentListPresenter.setProjectStats(projectStats);
            // re-use these stats for the project stats
            Log.info("Time to calculate project stats: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
            eventBus.fireEvent(new ProjectStatsUpdatedEvent(projectStats));

            exceptionHandler.setAppPresenter(appPresenter);
            exceptionHandler.setTargetContentsPresenter(injector.getTargetContentsPresenter());
         }
      });
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

   public static String getProjectHomeURL(WorkspaceId workspaceId)
   {
      return getModuleParentBaseUrl() + "project/view/" + workspaceId.getProjectIterationId().getProjectSlug();
   }
   
   public static String getVersionHomeURL(WorkspaceId workspaceId)
   {
      return getModuleParentBaseUrl() + "iteration/view/" + workspaceId.getProjectIterationId().getProjectSlug() + "/" + workspaceId.getProjectIterationId().getIterationSlug()  ;
   }

   public static String getVersionFilesURL(WorkspaceId workspaceId)
   {
      return getModuleParentBaseUrl() + "iteration/files/" + workspaceId.getProjectIterationId().getProjectSlug() + "/" + workspaceId.getProjectIterationId().getIterationSlug() + "/" + workspaceId.getLocaleId().getId();
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
    */
   private static void showErrorWithLink(String message, Throwable e)
   {
      Label messageLabel = new Label(message);
      messageLabel.getElement().addClassName(APP_LOAD_ERROR_CSS_CLASS);
      FlowPanel layoutPanel = new FlowPanel();
      layoutPanel.add(messageLabel);

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
}
