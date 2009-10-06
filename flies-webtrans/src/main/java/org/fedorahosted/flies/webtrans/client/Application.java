package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.gin.WebTransGinjector;
import org.fedorahosted.flies.webtrans.client.mvp.AppPresenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint, ResizeHandler {

	private static Application singleton;

	private final WebTransGinjector injector = GWT.create(WebTransGinjector.class);

	public void onModuleLoad() {
		final AppPresenter appPresenter = injector.getAppPresenter();
		appPresenter.go(RootPanel.get());

		injector.getPlaceManager().fireCurrentPlace();
	
		
		// Hook the window resize event, so that we can adjust the UI.
		Window.addResizeHandler(this);

		Window.enableScrolling(false);
		Window.setMargin("0px");

		// Call the window resized handler to get the initial sizes setup. Doing
		// this in a deferred command causes it to occur after all widgets'
		// sizes
		// have been computed by the browser.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onWindowResized(Window.getClientWidth(), Window
						.getClientHeight());
			}
		});

		onWindowResized(Window.getClientWidth(), Window.getClientHeight());
	}

	public void onResize(ResizeEvent event) {
		onWindowResized(event.getWidth(), event.getHeight());
	}

	public void onWindowResized(int width, int height) {
	    //appContainer.setWidth(width < 600 ? 600 : width) ;
	    //appContainer.setHeight(height < 400 ? 400 : height);
	}
}
