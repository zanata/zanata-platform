package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.auth.AuthenticationError;
import org.fedorahosted.flies.gwt.rpc.AuthenticateAction;
import org.fedorahosted.flies.gwt.rpc.AuthenticateResult;
import org.fedorahosted.flies.webtrans.client.NotificationEvent.Severity;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.auth.IdentityImpl;
import org.fedorahosted.flies.webtrans.client.auth.LoginResult;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.rpc.ErrorHandler;
import org.fedorahosted.flies.webtrans.client.rpc.SeamDispatchAsync;

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
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class LoginPresenter extends WidgetPresenter<LoginPresenter.Display> {

	public static final Place PLACE = new Place("LoginPanel");
	
	public interface Display extends WidgetDisplay {
		Button getLoginButton();
		TextBox getUsernameField();
		PasswordTextBox getPasswordField();
		void show();
		void hide();
		void showError();
	}

	private final Identity identity;
	private final CachingDispatchAsync dispatcher;
	
	@Inject
	public LoginPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, Identity identity) {
		super(display, eventBus);
		this.identity = identity;
		this.dispatcher = dispatcher;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		
		dispatcher.setErrorHandler(new ErrorHandler() {
			@Override
			public <A extends Action<R>, R extends Result> void onFailure(final A action,
					final AsyncCallback<R> callback, Throwable caught) {
				if(caught instanceof AuthenticationError) {
					doLogin(new LoginResult() {
						@Override
						public void onSuccess() {
							dispatcher.execute(action, callback);
						}
						
						@Override
						public void onFailure() {
						}
					});
				}
				else{
					callback.onFailure(caught);
				}
			}
		});
		
		
		registerHandler( display.getLoginButton().addClickHandler(clickHandler) );
		registerHandler( display.getUsernameField().addKeyUpHandler(keyHandler) );
		registerHandler( display.getPasswordField().addKeyUpHandler(keyHandler) );
	}

	private LoginResult callback;
	public void doLogin(final LoginResult callback) {
		this.callback = callback;
		display.show();
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
		identity.login(display.getUsernameField().getText(), display.getPasswordField().getText(), new LoginResult() {
			@Override
			public void onSuccess() {
				display.stopProcessing();
				display.hide();
				if(callback != null)
					callback.onSuccess();
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
