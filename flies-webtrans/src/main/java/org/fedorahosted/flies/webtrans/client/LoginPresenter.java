package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.rpc.AuthenticateAction;
import org.fedorahosted.flies.gwt.rpc.AuthenticateResult;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.auth.LoginResult;

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

	public static final Place PLACE = new Place("LoginPanel");
	
	public interface Display extends WidgetDisplay {
		Button getLoginButton();
		Button getCancelButton();
		TextBox getUsernameField();
		PasswordTextBox getPasswordField();
		void show();
		void hide();
		void showError();
	}

	private final Identity identity;
	
	@Inject
	public LoginPresenter(Identity identity, Display display, EventBus eventBus) {
		super(display, eventBus);
		this.identity = identity;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		
		display.getLoginButton().addClickHandler(clickHandler);
		display.getCancelButton().addClickHandler(clickHandler);
		display.getUsernameField().addKeyUpHandler(keyHandler);
		display.getPasswordField().addKeyUpHandler(keyHandler);
		eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler() {
			
			@Override
			public void onNotification(NotificationEvent event) {
				if (event.getSeverity() == Severity.Error) {
					display.show();
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
			if(event.getSource() == display.getLoginButton()) {
				tryLogin();
			}
			else{
				display.hide();
			}
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
		identity.login(display.getUsernameField().getText(), display.getPasswordField().getText(), new LoginResult() {
			@Override
			public void onSuccess() {
				display.stopProcessing();
				display.hide();
			}
			
			@Override
			public void onFailure() {
				display.stopProcessing();
				display.showError();
			}
		});
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
