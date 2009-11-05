package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.place.PlaceManager;
import net.customware.gwt.presenter.client.place.PlaceRequestEvent;

import org.fedorahosted.flies.webtrans.client.auth.LoginResult;
import org.fedorahosted.flies.webtrans.client.gin.WebTransGinjector;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint{

	private final WebTransGinjector injector = GWT.create(WebTransGinjector.class);

	public void onModuleLoad() {

		final AppPresenter appPresenter = injector.getAppPresenter();
		appPresenter.bind();
		RootPanel.get().add( appPresenter.getDisplay().asWidget() );
		
        // Needed because of this bug:
        // http://code.google.com/p/gwt-presenter/issues/detail?id=6
        PlaceManager placeManager = injector.getPlaceManager();
        injector.getEventBus().addHandler( PlaceRequestEvent.getType(), placeManager );

		injector.getPlaceManager().fireCurrentPlace();
		
		// Hook the window resize event, so that we can adjust the UI.
		Window.addResizeHandler( new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				injector.getEventBus().fireEvent( new WindowResizeEvent(event));
			}
		});

		Window.enableScrolling(false);
		Window.setMargin("0px");

		// Call the window resized handler to get the initial sizes setup. Doing
		// this in a deferred command causes it to occur after all widgets'
		// sizes
		// have been computed by the browser.
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				injector.getEventBus().fireEvent( new WindowResizeEvent(Window.getClientWidth(), Window
						.getClientHeight()));
			}
		});
	}
	
	// we reuse the logic of the generic ResizeEvent here
	// the only ResizeEvent allowed on the EventBus is the
	// window resize event
	public static class WindowResizeEvent extends ResizeEvent{
		WindowResizeEvent(int width, int height) {
			super(width, height);
		}
		public WindowResizeEvent(ResizeEvent event) {
			super(event.getWidth(), event.getHeight());
		}
	}
}
