package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class StatusBar extends Composite {

	public static enum CountUnit {
		Word, TranslationUnit;
	}
	
	private static StatusBarUiBinder uiBinder = GWT
			.create(StatusBarUiBinder.class);

	interface StatusBarUiBinder extends UiBinder<Widget, StatusBar> {
	}
	
	@UiField
	LayoutPanel layoutPanel;
	
	@UiField
	FlowPanel completePanel, inProgressPanel, unfinishedPanel, undefinedPanel;
	
	public StatusBar() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setStatus(int complete, int inProgress, int unfinished) {
		int total = complete + inProgress + unfinished;
		int width = getOffsetWidth();
		layoutPanel.forceLayout();
		if(total == 0) {
			layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 100, Unit.PC);
			layoutPanel.setWidgetLeftWidth(completePanel, 0.0, Unit.PX, 0, Unit.PX);
			layoutPanel.setWidgetLeftWidth(inProgressPanel, 0.0, Unit.PX, 0, Unit.PX);
			layoutPanel.setWidgetLeftWidth(unfinishedPanel, 0.0, Unit.PX, 0, Unit.PX);
		}
		else{
			int completePx = complete * 100 / total * width /100;
			int inProgressPx = inProgress * 100 / total * width /100;;
			int unfinishedPx = unfinished * 100 / total * width /100;;
			
			layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 0, Unit.PX);
			
			layoutPanel.setWidgetLeftWidth(completePanel, 0.0, Unit.PX, completePx, Unit.PX);
			layoutPanel.setWidgetLeftWidth(inProgressPanel, completePx, Unit.PX, inProgressPx, Unit.PX);
			layoutPanel.setWidgetLeftWidth(unfinishedPanel, completePx+inProgressPx, Unit.PX, unfinishedPx, Unit.PX);
		}
		
		layoutPanel.animate(1000);
	}
	
}
