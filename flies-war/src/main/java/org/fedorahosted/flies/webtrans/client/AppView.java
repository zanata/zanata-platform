package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocumentInfo;
import org.fedorahosted.flies.webtrans.client.ui.HasPager;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount;

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

	interface AppViewUiBinder extends UiBinder<DockLayoutPanel, AppView> {
	}

	private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

	@UiField
	Anchor signOutLink, leaveLink, helpLink, documentsLink;

	@UiField
	SpanElement user, workspaceName, workspaceLocale, selectedDocumentSpan;

	@UiField
	LayoutPanel editor;
	
	@UiField(provided = true)
	final Resources resources;
	
	@UiField
	DockLayoutPanel editorPanel;
	
	@UiField(provided=true)
	SidePanel sidePanel;
	
	@UiField
	SplitLayoutPanel mainSplitPanel;
	
	@UiField
	FlowPanel tmPanel, transUnitNavigation;
	
	@UiField(provided=true) 
	Pager pager;
	
	@UiField(provided=true)
	TransUnitCountBar transUnitCountBar;
	
	private Widget documentListView;
	private Widget editorView;
	private Widget filterView;
	private Widget workspaceUsersView;
	
	final WebTransMessages messages;
	
	@Inject
	public AppView(Resources resources, WebTransMessages messages, SidePanel sidePanel) {
		this.resources = resources;
		this.messages = messages;
		this.sidePanel = sidePanel;
		this.pager = new Pager(resources);
		this.transUnitCountBar = new TransUnitCountBar(messages);

		initWidget(uiBinder.createAndBindUi(this));
		mainSplitPanel.setWidgetMinSize(sidePanel, 200);
		mainSplitPanel.setWidgetMinSize(tmPanel, 150);
		
		
		user.setInnerText("Bob the Builder");
		workspaceName.setInnerText("Deployment Guide (F13)");
		workspaceLocale.setInnerText("German");
		pager.setVisible(false);
		transUnitNavigation.setVisible(false);
		
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
			editor.setWidgetTopBottom(documentListView, 0, Unit.PX, 0, Unit.PX);
			editor.setWidgetTopHeight(editorView, 0, Unit.PX, 0, Unit.PX);
			pager.setVisible(false);
			transUnitNavigation.setVisible(false);
			break;
		case Editor:
			editor.setWidgetTopBottom(editorView, 0, Unit.PX, 0, Unit.PX);
			editor.setWidgetTopHeight(documentListView, 0, Unit.PX, 0, Unit.PX);
			pager.setVisible(true);
			transUnitNavigation.setVisible(true);
			break;
		}
	}

	@Override
	public void setDocumentListView(Widget documentListView) {
		this.editor.add(documentListView);
		this.documentListView = documentListView;
	}
	
	@Override
	public void setEditorView(Widget editorView) {
		this.editor.add(editorView);
		this.editorView = editorView;
	}
	
	@Override
	public void setTransUnitNavigationView(Widget transUnitNavigation) {
		this.transUnitNavigation.clear();
		this.transUnitNavigation.add(transUnitNavigation);
	}
	
	@Override
	public void setTranslationMemoryView(Widget translationMemoryView) {
		tmPanel.clear();
		tmPanel.add(translationMemoryView);
	}
	
	@Override
	public HasPager getTableEditorPager() {
		return pager;
	}
	
	@Override
	public void setFilterView(Widget filterView) {
		sidePanel.setFilterView(filterView);
	}

	@Override
	public HasTransUnitCount getTransUnitCountBar() {
		return transUnitCountBar;
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
	public void setTableEditorPagerVisible(boolean visible) {
		pager.setVisible(visible);
	}
	
	@Override
	public void setTransUnitCountBarVisible(boolean visible) {
		transUnitCountBar.setVisible(visible);
	}
	
	@Override
	public void setUserLabel(String userLabel) {
		user.setInnerText(userLabel);
	}
	
	@Override
	public void setWorkspaceNameLabel(String workspaceNameLabel) {
		workspaceName.setInnerText(workspaceNameLabel);
	}
	
	@Override
	public void setLocaleLabel(String localeLabel) {
		workspaceLocale.setInnerText(localeLabel);
	}
	
	@Override
	public void setSelectedDocument(DocumentInfo document) {
		selectedDocumentSpan.setInnerText(document.getName());
	}
}
