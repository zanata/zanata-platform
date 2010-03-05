package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TopMenuView extends Composite implements TopMenuPresenter.Display {

	interface TopMenuUiBinder extends UiBinder<Widget, TopMenuView> {
	}

	private static TopMenuUiBinder uiBinder = GWT.create(TopMenuUiBinder.class);

	@UiField
	Label userLabel, workspaceLabel;

	@UiField
	FlowPanel transUnitNavigationPanel;

	public TopMenuView() {
		initWidget(uiBinder.createAndBindUi(this));
		userLabel.setText("<Username>");
		workspaceLabel.setText("Workspace");

	}

	@Override
	public void setTransUnitNavigation(Widget transUnitNavigation) {
		this.transUnitNavigationPanel.clear();
		this.transUnitNavigationPanel.add(transUnitNavigation);
	}
	
	@Override
	public HasText getProjectName() {
		return workspaceLabel;
	}

	@Override
	public HasText getUsername() {
		return userLabel;
	}


	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public HasWidgets getWidgets() {
		return (HasWidgets)this.getWidget();
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}

}
