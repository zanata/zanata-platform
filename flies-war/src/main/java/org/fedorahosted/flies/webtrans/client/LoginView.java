package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.GlassPanel;

public class LoginView extends DecoratedPopupPanel implements LoginPresenter.Display {

	private final TextBox username;
	private final PasswordTextBox password;
	private final Button loginButton;
	private final Label errorLabel;

	private final GlassPanel glassPanel;
	
	public LoginView() {

		FlexTable layout = new FlexTable();
		layout.setWidth("400px");
		layout.setCellPadding(6);

		username = new TextBox();
		password = new PasswordTextBox();
		loginButton = new Button("Login");
		glassPanel = new GlassPanel(false);
		errorLabel = new Label("Invalid Username / Password combination.");
		errorLabel.setVisible(false);
		layout.setHTML(0, 1, "Welcome to Flies");
		layout.setHTML(1, 0, "Username");
		layout.setWidget(1, 1, username);
		layout.setHTML(2, 0, "Password");
		layout.setWidget(2, 1, password);
		layout.setWidget(3, 2, loginButton);
		layout.setWidget(3, 1, errorLabel);

		setAnimationEnabled(true);
		setModal(true);
		add(layout);
	}
	
	@Override
	public void showError() {
		errorLabel.setVisible(true);
	}

	@Override
	public void show() {
		RootPanel.get().setSize("100%", "100%");
		RootPanel.get().add( glassPanel, 0, 0);
		int w = RootPanel.get().getOffsetWidth();
		int h = RootPanel.get().getOffsetHeight();
		super.show();
		setPopupPosition( (w/2) - (getOffsetWidth()/2), (h/2) - (getOffsetHeight()/2) );
	}
	
	@Override
	public void hide() {
		glassPanel.removeFromParent();
		super.hide();
	}


	@Override
	public Button getLoginButton() {
		return loginButton;
	}

	@Override
	public PasswordTextBox getPasswordField() {
		return password;
	}


	@Override
	public TextBox getUsernameField() {
		return username;
	}


	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		enableAll(false);
	}

	@Override
	public void stopProcessing() {
		enableAll(true);
	}

	private void enableAll(boolean enable){
		loginButton.setEnabled(enable);
		username.setEnabled(enable);
		password.setEnabled(enable);
	}
}
