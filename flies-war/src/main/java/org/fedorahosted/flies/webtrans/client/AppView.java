package org.fedorahosted.flies.webtrans.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class AppView extends Composite implements AppPresenter.Display {

	interface AppViewUiBinder extends UiBinder<DockLayoutPanel, AppView> {
	}

	private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);
	
	@UiField
	AnchorElement signOutLink, leaveLink, helpLink;
	
	@UiField
	SpanElement user, workspaceName, workspaceLocale;
	
	@UiField
	FlowPanel editor;
	
	public AppView() {
		initWidget(uiBinder.createAndBindUi(this));
		user.setInnerText("Bob the Builder");
		workspaceName.setInnerText("Deployment Guide (F13)");
		workspaceLocale.setInnerText("German");
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
}
