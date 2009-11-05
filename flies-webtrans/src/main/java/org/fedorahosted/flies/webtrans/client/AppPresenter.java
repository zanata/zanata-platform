package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.Application.WindowResizeEvent;
import org.fedorahosted.flies.webtrans.client.LoginPresenter.LoggedIn;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.auth.LoginResult;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppPresenter extends WidgetPresenter<AppPresenter.Display> {
	
	public interface Display extends WidgetDisplay {
		public void setWest(Widget west);
		public void setMain(Widget main);
		public void setNorth(Widget north);
	}
	
	private final WestNavigationPresenter westNavigationPresenter;
	private final WebTransEditorPresenter webTransEditorPresenter;
	private final TopMenuPresenter topMenuPresenter;
	private final EventProcessor eventProcessor;
	private final LoginPresenter loginPresenter;
	
	@Inject
	public AppPresenter(Display display, EventBus eventBus,
				final WestNavigationPresenter leftNavigationPresenter,
				final WebTransEditorPresenter webTransEditorPresenter,
				final TopMenuPresenter topMenuPresenter,
				final EventProcessor eventProcessor,
				final LoginPresenter loginPresenter) {
		super(display, eventBus);
		
		this.westNavigationPresenter = leftNavigationPresenter;
		this.webTransEditorPresenter = webTransEditorPresenter;
		this.topMenuPresenter = topMenuPresenter;
		this.eventProcessor = eventProcessor;
		this.loginPresenter = loginPresenter;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	protected void bindApp() {
		westNavigationPresenter.bind();
		webTransEditorPresenter.bind();
		topMenuPresenter.bind();
		
		eventProcessor.scheduleRepeating(3000);
		
		display.setNorth(topMenuPresenter.getDisplay().asWidget());
		display.setWest(westNavigationPresenter.getDisplay().asWidget());
		display.setMain(webTransEditorPresenter.getDisplay().asWidget());
		// TODO refactor to presenter
		
		registerHandler(
			eventBus.addHandler(WindowResizeEvent.getType(), new ResizeHandler() {
				@Override
				public void onResize(ResizeEvent event) {
					display.asWidget().setHeight(event.getHeight() + "px");
					display.asWidget().setWidth(event.getWidth() + "px");
				}
			})
		);
		
		registerHandler(
			eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler() {
				
				@Override
				public void onNotification(NotificationEvent event) {
					PopupPanel popup = new PopupPanel(true);
					popup.addStyleDependentName("Notification");
					popup.addStyleName("Severity-"+ event.getSeverity().name());
					Widget center = webTransEditorPresenter.getDisplay().asWidget();
					popup.setWidth(center.getOffsetWidth()-40 + "px");
					popup.setWidget(new Label(event.getMessage()));
					popup.setPopupPosition(center.getAbsoluteLeft()+20, center.getAbsoluteTop()+30);
					popup.show();
				}
			})
		);
		
	}
	
	@Override
	protected void onBind() {
		loginPresenter.bind();
		loginPresenter.ensureLoggedIn(new LoggedIn() {
			@Override
			public void onSuccess() {
				bindApp();
			}
		});
		
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
		westNavigationPresenter.unbind();
		webTransEditorPresenter.unbind();
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
	}
}
