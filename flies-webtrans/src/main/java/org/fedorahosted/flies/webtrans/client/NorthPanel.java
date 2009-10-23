package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.Person;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class NorthPanel extends HorizontalPanel {
	
	private final Label userLabel;
	private final Label notLoggedInLabel;
	private final Hyperlink loginLink;
	
	private final HorizontalPanel rightMenu;
	
	public NorthPanel() {
		setHeight("20px");
		setWidth("100%");
		
		rightMenu = new HorizontalPanel();
		rightMenu.setSpacing(5);
		userLabel = new Label("<Username>");
		userLabel.setVisible(false);
		
		notLoggedInLabel = new Label("Not Logged In");
		
		loginLink = new Hyperlink("Login", "LoginPanel");
		
		rightMenu.add(notLoggedInLabel);
		rightMenu.add(userLabel);
		rightMenu.add(loginLink);
		
		add(rightMenu);
		setCellHorizontalAlignment(rightMenu, HorizontalPanel.ALIGN_RIGHT);
	}
	
	
	public void setUser(Person person) {
		userLabel.setText(person.getName());
		notLoggedInLabel.setVisible(false);
		userLabel.setVisible(true);
	}
	
	public void clearUser(){
		userLabel.setVisible(false);
		notLoggedInLabel.setVisible(true);
	}
	
	

	
}
