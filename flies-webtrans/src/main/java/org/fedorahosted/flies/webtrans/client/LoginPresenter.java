package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.rpc.AuthenticateAction;
import org.fedorahosted.flies.gwt.rpc.AuthenticateResult;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class LoginPresenter extends WidgetPresenter<LoginPresenter.Display> {

	public interface Display extends WidgetDisplay {
		Button getLoginButton();
		TextBox getUsernameField();
		PasswordTextBox getPasswordField();
		void show();
		void hide();
	}

	private final DispatchAsync dispatcher;
	
	@Inject
	public LoginPresenter(DispatchAsync dispatcher, Display display, EventBus eventBus) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		
		display.getLoginButton().addClickHandler(clickHandler);
		display.getUsernameField().addKeyUpHandler(keyHandler);
		display.getPasswordField().addKeyUpHandler(keyHandler);
		eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler() {
			
			@Override
			public void onNotification(NotificationEvent event) {
				if (event.getSeverity() == Severity.Error) {
					//display.show();
				}
			}
		});
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	private final ClickHandler clickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			tryLogin();
		}
	};
	
	private final KeyUpHandler keyHandler = new KeyUpHandler() {
		
		@Override
		public void onKeyUp(KeyUpEvent event) {
			if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
				tryLogin();
			}
		}
	};

	private void tryLogin() {
		display.startProcessing();

		dispatcher.execute(new AuthenticateAction(display.getUsernameField().getText(), display.getPasswordField().getText()), new AsyncCallback<AuthenticateResult>() {
			
			@Override
			public void onSuccess(AuthenticateResult result) {
				display.hide();
				Cookies.setCookie("JSESSIONID", result.getSessionId());
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
		
		display.stopProcessing();
	}
	
	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
		display.show();
	}
	
}
