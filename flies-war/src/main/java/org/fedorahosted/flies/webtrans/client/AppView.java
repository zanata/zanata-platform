package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocumentInfo;
import org.fedorahosted.flies.webtrans.client.ui.HasPager;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount.CountUnit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.gen2.logging.shared.Log;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppPresenter.Display {

	interface AppViewUiBinder extends UiBinder<LayoutPanel, AppView> {
	}

	private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

	@UiField
	Anchor signOutLink, leaveLink, helpLink, documentsLink;

	@UiField
	SpanElement user, selectedDocumentSpan, selectedDocumentPathSpan;

	@UiField
	LayoutPanel editorContainer, sidePanelContainer, sidePanelOuterContainer;
	
	@UiField(provided = true)
	final Resources resources;
	
	@UiField
	SplitLayoutPanel mainSplitPanel;
	
	private Widget documentListView;
	private Widget editorView;
	
	final WebTransMessages messages;
	
	@Inject
	public AppView(Resources resources, WebTransMessages messages) {
		this.resources = resources;
		this.messages = messages;

		initWidget(uiBinder.createAndBindUi(this));
		mainSplitPanel.setWidgetMinSize(sidePanelOuterContainer, 200);
		
		helpLink.setHref( messages.hrefHelpLink() );
		helpLink.setTarget("_BLANK");
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
	public void showInMainView(MainView view) {
		switch(view) {
		case Documents:
			editorContainer.setWidgetTopBottom(documentListView, 0, Unit.PX, 0, Unit.PX);
			editorContainer.setWidgetTopHeight(editorView, 0, Unit.PX, 0, Unit.PX);
			break;
		case Editor:
			editorContainer.setWidgetTopBottom(editorView, 0, Unit.PX, 0, Unit.PX);
			editorContainer.setWidgetTopHeight(documentListView, 0, Unit.PX, 0, Unit.PX);
			break;
		}
	}

	@Override
	public void setDocumentListView(Widget documentListView) {
		this.editorContainer.add(documentListView);
		this.documentListView = documentListView;
	}
	
	@Override
	public void setEditorView(Widget editorView) {
		this.editorContainer.add(editorView);
		this.editorView = editorView;
	}

	@Override
	public void setSidePanel(Widget sidePanel) {
		sidePanelContainer.clear();
		sidePanelContainer.add(sidePanel);
	}
	
	@Override
	public HasClickHandlers getHelpLink() {
		return helpLink;
	}
	
	@Override
	public HasClickHandlers getLeaveWorkspaceLink() {
		return leaveLink;
	}
	
	@Override
	public HasClickHandlers getSignOutLink() {
		return signOutLink;
	}

	@Override
	public HasClickHandlers getDocumentsLink() {
		return documentsLink;
	}
	
	@Override
	public void setUserLabel(String userLabel) {
		user.setInnerText(userLabel);
	}
	
	@Override
	public void setWorkspaceNameLabel(String workspaceNameLabel) {
		documentsLink.setText(workspaceNameLabel);
	}
	
	@Override
	public void setSelectedDocument(DocumentInfo document) {
		String path = document.getPath() == null || document.getPath().isEmpty() ? "" : document.getPath() + "/";
		selectedDocumentPathSpan.setInnerText(path);
		selectedDocumentSpan.setInnerText(document.getName());
	}
}
