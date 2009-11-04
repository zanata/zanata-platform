package org.fedorahosted.flies.webtrans.editor;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class WebTransEditorMenubar extends FlowPanel implements HasThreeColWidgets {
	
	private final FlowPanel leftWidget;
	private final FlowPanel middleWidget;
	private final FlowPanel rightWidget;
	
	public WebTransEditorMenubar() {
		setStyleName("WebTransEditorMenu");
		leftWidget = new FlowPanel();
		leftWidget.setStyleName("WebTransEditorMenu-left");
		middleWidget = new FlowPanel();
		middleWidget.setStyleName("WebTransEditorMenu-middle");
		rightWidget = new FlowPanel();
		rightWidget.setStyleName("WebTransEditorMenu-right");
		
		add(leftWidget);
		add(middleWidget);
		add(rightWidget);
	}
	
	public void setLeftWidget(Widget leftWidget) {
		if(this.leftWidget.getWidgetCount() != 0)
			this.leftWidget.remove(0);
		this.leftWidget.add(leftWidget);
	}
	
	public void setMiddleWidget(Widget middleWidget) {
		if(this.middleWidget.getWidgetCount() != 0)
			this.middleWidget.remove(0);
		this.middleWidget.add(middleWidget);
	}
	
	public void setRightWidget(Widget rightWidget) {
		if(this.rightWidget.getWidgetCount() != 0)
			this.rightWidget.remove(0);
		this.rightWidget.add(rightWidget);
	}
	
}
