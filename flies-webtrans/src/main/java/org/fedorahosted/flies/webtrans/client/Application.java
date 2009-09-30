package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint, ResizeHandler {

	private static Application singleton;

	/**
	 * Gets the singleton Application instance.
	 */
	public static Application get() {
		return singleton;
	}

	private RightPanel rightPanel = new RightPanel();
	private LeftPanel leftPanel = new LeftPanel();
	private TranslationPanel translationPanel = new TranslationPanel();

	/**
	 * This method constructs the application user interface by instantiating
	 * controls and hooking up event handler.
	 */
	public void onModuleLoad() {
		singleton = this;
		leftPanel.setWidth("250px");
		rightPanel.setWidth("250px");
		
		translationPanel.setWidth("100%");
		
		// Create a dock panel that will contain the menu bar at the top,
		// the shortcuts to the left, and the mail list & details taking the
		// rest.
		DockPanel outer = new DockPanel();
		outer.add(leftPanel, DockPanel.WEST);
		outer.add(rightPanel, DockPanel.EAST);
		outer.add(translationPanel, DockPanel.CENTER);
		outer.setWidth("100%");
		outer.setSpacing(4);
		outer.setBorderWidth(2);

		// Hook the window resize event, so that we can adjust the UI.
		Window.addResizeHandler(this);

		// Get rid of scrollbars, and clear out the window's built-in margin,
		// because we want to take advantage of the entire client area.
		Window.enableScrolling(false);
		Window.setMargin("0px");

		// Finally, add the outer panel to the RootPanel, so that it will be
		// displayed.
		RootPanel.get().add(outer);

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
	    // Adjust the shortcut panel and detail area to take up the available room
	    // in the window.
	    int shortcutHeight = height - leftPanel.getAbsoluteTop() - 8;
	    if (shortcutHeight < 1) {
	      shortcutHeight = 1;
	    }
	    leftPanel.setHeight(shortcutHeight + "px");
	    translationPanel.setHeight(shortcutHeight + "px");
	    leftPanel.setWidth("200px");
	    rightPanel.setWidth("200px");
	    translationPanel.setWidth(width - 400 + "px");
	    rightPanel.setHeight(shortcutHeight + "px");
	    // Give the mail detail widget a chance to resize itself as well.
//	    mailDetail.adjustSize(width, height);
	}
}
