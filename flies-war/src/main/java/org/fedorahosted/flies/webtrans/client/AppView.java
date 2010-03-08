package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.gen2.logging.shared.Log;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppPresenter.Display {

	interface AppViewUiBinder extends UiBinder<DockLayoutPanel, AppView> {
	}

	private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

	@UiField
	Anchor signOutLink, leaveLink, helpLink, documentsLink;

	@UiField
	SpanElement user, workspaceName, workspaceLocale;

	@UiField
	FlowPanel editor;

	@UiField(provided = true)
	final Resources resources;

	@Inject
	public AppView(Resources resources) {
		this.resources = resources;
		initWidget(uiBinder.createAndBindUi(this));
		user.setInnerText("Bob the Builder");
		workspaceName.setInnerText("Deployment Guide (F13)");
		workspaceLocale.setInnerText("German");
		SplitLayoutPanel p = new SplitLayoutPanel();
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}

	@Override
	public void setEditor(Widget editor) {
		this.editor.clear();
		this.editor.add(editor);
	}

	@UiHandler("signOutLink")
	void handleSignOutClick(ClickEvent e) {
		Log.info("sign out clicked");
	}

	@UiHandler("leaveLink")
	void handleLeaveClick(ClickEvent e) {
		Log.info("leave clicked");
	}

	@UiHandler("helpLink")
	void handleHelpClick(ClickEvent e) {
		Log.info("help clicked");
	}

	@UiHandler("documentsLink")
	void handleDocumentsClick(ClickEvent e) {
		Log.info("documents clicked");
	}

}
