package net.openl10n.flies.webtrans.client;

import net.customware.gwt.presenter.client.place.PlaceManager;
import net.customware.gwt.presenter.client.place.PlaceRequestEvent;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.webtrans.client.EventProcessor.StartCallback;
import net.openl10n.flies.webtrans.client.gin.WebTransGinjector;
import net.openl10n.flies.webtrans.shared.NoSuchWorkspaceException;
import net.openl10n.flies.webtrans.shared.auth.AuthenticationError;
import net.openl10n.flies.webtrans.shared.auth.AuthorizationError;
import net.openl10n.flies.webtrans.shared.auth.Identity;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;
import net.openl10n.flies.webtrans.shared.model.WorkspaceContext;
import net.openl10n.flies.webtrans.shared.model.WorkspaceId;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceResult;
import net.openl10n.flies.webtrans.shared.rpc.ExitWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ExitWorkspaceResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint
{

   private static String fliesUrl = null;
   private static WorkspaceId workspaceId;
   private static WorkspaceContext workspaceContext;
   private static Identity identity;

   private final WebTransGinjector injector = GWT.create(WebTransGinjector.class);

   public void onModuleLoad()
   {

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
               showError("Invalid Workspace");
            }
            catch (Throwable e)
            {
               Log.error("An unexpected Error occurred", e);
               showError("An unexpected Error occurred: " + e.getMessage());
            }
         }

         @Override
         public void onSuccess(ActivateWorkspaceResult result)
         {
            workspaceContext = result.getWorkspaceContext();
            identity = result.getIdentity();
            injector.getDispatcher().setIdentity(identity);
            injector.getDispatcher().setWorkspaceContext(workspaceContext);
            startApp();
         }

      });
   }

   private void startApp()
   {

      // When user close the workspace, send ExitWorkSpaceAction
      Window.addCloseHandler(new CloseHandler<Window>()
      {
         @Override
         public void onClose(CloseEvent<Window> event)
         {
            // injector.getDispatcher().execute(new ExitWorkspaceAction(),
            // new AsyncCallback<ExitWorkspaceResult>() {
            // @Override
            // public void onFailure(Throwable caught) {
            //
            // }
            //
            // @Override
            // public void onSuccess(ExitWorkspaceResult result) {
            // }
            //
            // });
         }
      });

      final EventProcessor eventProcessor = injector.getEventProcessor();
      eventProcessor.start(new StartCallback()
      {
         @Override
         public void onSuccess()
         {
            delayedStartApp();
         }

         @Override
         public void onFailure(Throwable e)
         {
            RootLayoutPanel.get().add(new HTML("<h1>Failed to start Event Service...</h1>" + "<b>Exception:</b> " + e.getMessage()));
         }
      });

   }

   private void delayedStartApp()
   {
      final AppPresenter appPresenter = injector.getAppPresenter();
      RootLayoutPanel.get().add(appPresenter.getDisplay().asWidget());
      appPresenter.bind();

      // Needed because of this bug:
      // http://code.google.com/p/gwt-presenter/issues/detail?id=6
      PlaceManager placeManager = injector.getPlaceManager();
      injector.getEventBus().addHandler(PlaceRequestEvent.getType(), placeManager);

      injector.getPlaceManager().fireCurrentPlace();
   }

   public static ProjectIterationId getProjectIterationId()
   {
      String projectSlug = Window.Location.getParameter("project");
      String iterationSlug = Window.Location.getParameter("iteration");
      if (projectSlug == null || iterationSlug == null)
         return null;
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
      redirectToUrl(getModuleParentBaseUrl() + "account/sign_in?continue=" + URL.encodeComponent(Window.Location.getHref()));
   }

   public static void redirectToLogout()
   {
      redirectToUrl(getModuleParentBaseUrl() + "account/sign_out");
   }

   public static void redirectToFliesProjectHome(WorkspaceId workspaceId)
   {
      redirectToUrl(getModuleParentBaseUrl() + "project/view/" + workspaceId.getProjectIterationId().getProjectSlug());
   }

   public static native void redirectToUrl(String url)/*-{
                                                      $wnd.location = url;
                                                      }-*/;

   public static native void closeWindow()/*-{
                                          $wnd.close();
                                          }-*/;

   public static WorkspaceId getWorkspaceId()
   {
      if (workspaceId == null)
      {
         // TODO handle null values
         workspaceId = new WorkspaceId(getProjectIterationId(), getLocaleId());
      }
      return workspaceId;
   }

   public static WorkspaceContext getWorkspaceContext()
   {
      return workspaceContext;
   }

   public static Identity getIdentity()
   {
      return identity;
   }

   public static String getModuleParentBaseUrl()
   {
      return GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "");
   }

   public static void showError(String message)
   {
      Label label = new Label(message);
      RootLayoutPanel.get().add(label);
   }

}
