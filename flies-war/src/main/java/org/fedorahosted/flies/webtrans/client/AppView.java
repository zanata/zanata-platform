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
import com.google.gwt.user.client.ui.HTML;
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

	@UiField(provided=true)
	Widget editor;
	
	@UiField(provided = true)
	final Resources resources;

	@UiField
	DockLayoutPanel editorPanel;

	private Widget documentListView;
	private Widget editorView;
	private Widget widgetInEditorView;
	
	@Inject
	public AppView(Resources resources) {
		this.resources = resources;
		editor = new HTML("");
		widgetInEditorView = editor;
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
	public void showDocumentsView() {
		if(documentListView == null) {
			throw new RuntimeException("documentListView is not set");
		}
		else if(widgetInEditorView == documentListView) {
			return;
		}
		editorPanel.remove(widgetInEditorView);
		widgetInEditorView = documentListView;
		editorPanel.add(documentListView);
	}
	
	@Override
	public void showEditor() {
		if(editorView == null) {
			throw new RuntimeException("editorView is not set");
		}
		else if (widgetInEditorView == editorView ) {
			return;
		}
		editorPanel.remove(widgetInEditorView);
		widgetInEditorView = editorView;
		editorPanel.add(editorView);
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
		showDocumentsView();
	}

	@Override
	public void setDocumentListView(Widget documentListView) {
		this.documentListView = documentListView;
	}
	
	@Override
	public void setEditorView(Widget editorView) {
		this.editorView = editorView;
	}

}
