package org.fedorahosted.flies.webtrans.editor;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class WebTransEditorMenubar extends HorizontalPanel implements HasThreeColWidgets {
	
	private Widget leftWidget;
	private Widget middleWidget;
	private Widget rightWidget;
	
	public WebTransEditorMenubar() {
		setStylePrimaryName("WebTransEditor");
		addStyleDependentName("MenuBar");
		setHeight("20px");
		setWidth("100%");
		setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		this.leftWidget = new Label();
		this.middleWidget = new Label();
		this.rightWidget = new Label();
	}
	
	public void setLeftWidget(Widget leftWidget) {
		this.leftWidget.removeFromParent();
		this.leftWidget = leftWidget;
		insert(leftWidget, 0);
		setCellHorizontalAlignment(this.leftWidget, HorizontalPanel.ALIGN_LEFT);
	}
	
	public void setMiddleWidget(Widget middleWidget) {
		this.middleWidget.removeFromParent();
		this.middleWidget = middleWidget;
		insert(middleWidget, 1);
		setCellHorizontalAlignment(this.middleWidget, HorizontalPanel.ALIGN_CENTER);
	}
	
	public void setRightWidget(Widget rightWidget) {
		this.rightWidget.removeFromParent();
		this.rightWidget = rightWidget;
		insert(rightWidget, 2);
		setCellHorizontalAlignment(this.rightWidget, HorizontalPanel.ALIGN_RIGHT);
	}
	
}
