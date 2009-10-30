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
		setLeftWidget(new Label());
		setMiddleWidget(new Label());
		setRightWidget(new Label());
	}
	
	public void setLeftWidget(Widget leftWidget) {
		if(this.leftWidget != null)
			this.leftWidget.removeFromParent();
		this.leftWidget = leftWidget;
		insert(leftWidget, 0);
		setCellHorizontalAlignment(this.leftWidget, HorizontalPanel.ALIGN_LEFT);
	}
	
	public void setMiddleWidget(Widget middleWidget) {
		if(this.middleWidget != null)
			this.middleWidget.removeFromParent();
		this.middleWidget = middleWidget;
		insert(middleWidget, 1);
		setCellHorizontalAlignment(this.middleWidget, HorizontalPanel.ALIGN_CENTER);
	}
	
	public void setRightWidget(Widget rightWidget) {
		if(this.rightWidget != null)
			this.rightWidget.removeFromParent();
		this.rightWidget = rightWidget;
		insert(rightWidget, 2);
		setCellHorizontalAlignment(this.rightWidget, HorizontalPanel.ALIGN_RIGHT);
	}
	
}
