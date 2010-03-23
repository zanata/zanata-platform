package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.place.PlaceManager;
import net.customware.gwt.presenter.client.place.PlaceRequestEvent;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.auth.Identity;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceResult;
import org.fedorahosted.flies.webtrans.client.gin.WebTransGinjector;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
		
		
		final AppPresenter appPresenter = injector.getAppPresenter();
		RootLayoutPanel.get().add( appPresenter.getDisplay().asWidget() );
		appPresenter.bind();
		
        // Needed because of this bug:
        // http://code.google.com/p/gwt-presenter/issues/detail?id=6
        PlaceManager placeManager = injector.getPlaceManager();
        injector.getEventBus().addHandler( PlaceRequestEvent.getType(), placeManager );

		injector.getPlaceManager().fireCurrentPlace();
	}
	
	public static ProjectContainerId getProjectContainerId() {
		String projContainerId = Window.Location.getParameter("projContainerId");
		if(projContainerId == null)
			return null;
		try{
			int id = Integer.parseInt(projContainerId);
			return new ProjectContainerId(id);
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
		redirectToUrl( FLIES_BASE_PATH + "project/project_by_container_id.seam?id="+ workspaceId.getProjectContainerId().getId());	
	}
	
	public static native void redirectToUrl(String url)/*-{
		$wnd.location = url;
	}-*/;

	public static WorkspaceId getWorkspaceId() {
		if(workspaceId == null) {
			// TODO handle null values
			workspaceId = new WorkspaceId(getProjectContainerId(), getLocaleId());
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
