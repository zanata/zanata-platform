package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.place.PlaceManager;
import net.customware.gwt.presenter.client.place.PlaceRequestEvent;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.auth.Identity;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.ProjectIterationId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceResult;
import org.fedorahosted.flies.webtrans.client.EventProcessor.StartCallback;
import org.fedorahosted.flies.webtrans.client.gin.WebTransGinjector;

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
public class Application implements EntryPoint{

	public static final String FLIES_BASE_PATH = "/flies/";
	private static WorkspaceId workspaceId;
	private static WorkspaceContext workspaceContext;
	private static Identity identity;
	
	private final WebTransGinjector injector = GWT.create(WebTransGinjector.class);
	public void onModuleLoad() {
		
		injector.getDispatcher().execute(
			new ActivateWorkspaceAction( getWorkspaceId() ), new AsyncCallback<ActivateWorkspaceResult>() {

				@Override
				public void onFailure(Throwable caught) {
					redirectToLogin();
				}

				@Override
				public void onSuccess(ActivateWorkspaceResult result) {
					workspaceContext = result.getWorkspaceContext();
					identity = result.getIdentity();
					injector.getDispatcher().setIdentity(identity);
					injector.getDispatcher().setWorkspaceContext(workspaceContext);
					startApp();
				}
				
			}
		);
	}
	
	private void startApp() {
		
		// When user close the workspace, send ExitWorkSpaceAction
		Window.addCloseHandler(new CloseHandler<Window>() {
			@Override
			public void onClose(CloseEvent<Window> event) {
//				injector.getDispatcher().execute(new ExitWorkspaceAction(),
//						new AsyncCallback<ExitWorkspaceResult>() {
//							@Override
//							public void onFailure(Throwable caught) {
//
//							}
//
//							@Override
//							public void onSuccess(ExitWorkspaceResult result) {
//							}
//
//						});
			}
		});
		
		
		final EventProcessor eventProcessor = injector.getEventProcessor();
		eventProcessor.start( new StartCallback() {
			@Override
			public void onSuccess() {
				delayedStartApp();
			}
			
			@Override
			public void onFailure(Throwable e) {
				RootLayoutPanel.get().add( 
						new HTML("<h1>Failed to start Event Service...</h1>" 
						+ "<b>Exception:</b> "+ e.getMessage()));
			}
		});

	}
	
	private void delayedStartApp() {
		final AppPresenter appPresenter = injector.getAppPresenter();
		RootLayoutPanel.get().add( appPresenter.getDisplay().asWidget() );
		appPresenter.bind();

        // Needed because of this bug:
        // http://code.google.com/p/gwt-presenter/issues/detail?id=6
        PlaceManager placeManager = injector.getPlaceManager();
        injector.getEventBus().addHandler( PlaceRequestEvent.getType(), placeManager );

		injector.getPlaceManager().fireCurrentPlace();
	}
	
	public static ProjectIterationId getProjectIterationId() {
		String projectSlug = Window.Location.getParameter("project");
		String iterationSlug = Window.Location.getParameter("iteration");
		if(projectSlug == null || iterationSlug == null)
			return null;
		try{
			return new ProjectIterationId(projectSlug, iterationSlug);
		}
		catch(NumberFormatException nfe){
			return null;
		}
	}

	public static LocaleId getLocaleId() {
		String localeId = Window.Location.getParameter("localeId");
		return localeId == null ? null : new LocaleId(localeId);
	}

	public static void redirectToLogin() {
		redirectToUrl( FLIES_BASE_PATH + "account/sign_in?continue=" + URL.encodeComponent(Window.Location.getHref()));	
	}
	
	public static void redirectToLogout() {
		redirectToUrl( FLIES_BASE_PATH + "account/sign_out");	
	}

	public static void redirectToFliesProjectHome(WorkspaceId workspaceId) {
		redirectToUrl( FLIES_BASE_PATH + "project/view/"+ workspaceId.getProjectIterationId().getProjectSlug());	
	}
	
	public static native void redirectToUrl(String url)/*-{
		$wnd.location = url;
	}-*/;

	public static WorkspaceId getWorkspaceId() {
		if(workspaceId == null) {
			// TODO handle null values
			workspaceId = new WorkspaceId(getProjectIterationId(), getLocaleId());
		}
		return workspaceId;
	}
	
	public static WorkspaceContext getWorkspaceContext() {
		return workspaceContext;
	}
	
	public static Identity getIdentity() {
		return identity;
	}
	
}
