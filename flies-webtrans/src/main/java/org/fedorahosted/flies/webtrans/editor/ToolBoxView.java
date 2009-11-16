package org.fedorahosted.flies.webtrans.editor;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ToolBoxView extends FlowPanel implements ToolBoxPresenter.Display {

	public ToolBoxView() {
		setStyleName("ToolBoxView");
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
	public void setTerminologyView(Widget view) {
		add(view);
	}

}
