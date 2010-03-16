package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StatusBar extends Composite {

	public static enum CountUnit {
		Word, TranslationUnit;
	}
	
	public static enum LabelFormat {
		Percentage,Unit;
	}

	
	private static StatusBarUiBinder uiBinder = GWT
			.create(StatusBarUiBinder.class);

	interface StatusBarUiBinder extends UiBinder<Widget, StatusBar> {
	}

	private CountUnit countUnit = CountUnit.TranslationUnit;
	private LabelFormat labelFormat = LabelFormat.Percentage;
	private boolean toggleEnabled = true;
	private boolean labelVisible = true;
	
	@UiField
	LayoutPanel layoutPanel;
	
	@UiField
	FlowPanel approvedPanel, needReviewPanel, untranslatedPanel, undefinedPanel;
	
	@UiField
	Label label;

	int complete, inProgress, unfinished;
	
	private final WebTransMessages messages;
	
	@Inject
	public StatusBar(WebTransMessages messages) {
		this.messages = messages;
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setStatus(int complete, int inProgress, int unfinished) {
		this.complete = complete;
		this.inProgress = inProgress;
		this.unfinished = unfinished;
		refresh();
	}

	public void refresh() {
		int total = complete + inProgress + unfinished;
		int width = getOffsetWidth();
		layoutPanel.forceLayout();
		if(total == 0) {
			layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 100, Unit.PC);
			layoutPanel.setWidgetLeftWidth(approvedPanel, 0.0, Unit.PX, 0, Unit.PX);
			layoutPanel.setWidgetLeftWidth(needReviewPanel, 0.0, Unit.PX, 0, Unit.PX);
			layoutPanel.setWidgetLeftWidth(untranslatedPanel, 0.0, Unit.PX, 0, Unit.PX);
			label.setText("");
		}
		else{
			int completePx = complete * 100 / total * width /100;
			int inProgressPx = inProgress * 100 / total * width /100;;
			int unfinishedPx = unfinished * 100 / total * width /100;;
			
			layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 0, Unit.PX);
			
			layoutPanel.setWidgetLeftWidth(approvedPanel, 0.0, Unit.PX, completePx, Unit.PX);
			layoutPanel.setWidgetLeftWidth(needReviewPanel, completePx, Unit.PX, inProgressPx, Unit.PX);
			layoutPanel.setWidgetLeftWidth(untranslatedPanel, completePx+inProgressPx, Unit.PX, unfinishedPx, Unit.PX);

			switch(labelFormat) {
			case Percentage:
				label.setText( messages.statusBarLabelPercentage(complete*100/total, inProgress*100/total, unfinished*100/total));
				break;
			case Unit:
				label.setText( messages.statusBarLabelUnits(complete, inProgress, unfinished));
				break;
			}
		}

		layoutPanel.animate(1000);
	}
	
	public void setCountUnit(CountUnit countUnit) {
		this.countUnit = countUnit;
		refresh();
	}

	public CountUnit getCountUnit() {
		return countUnit;
	}
	
	public void setLabelFormat(LabelFormat labelFormat) {
		this.labelFormat = labelFormat;
		refresh();
	}
	
	public LabelFormat getLabelFormat() {
		return labelFormat;
	}
	
	public void setToggleEnabled(boolean toggleEnabled) {
		this.toggleEnabled = toggleEnabled;
	}
	
	public boolean isToggleEnabled() {
		return toggleEnabled;
	}
	
	public void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
		label.setVisible(labelVisible);
	}
	
	public boolean isLabelVisible() {
		return labelVisible;
	}
	
	@UiHandler("label")
	public void onLabelClick(ClickEvent event) {
		if(toggleEnabled) {
			switch(labelFormat) {
			case Percentage:
				setLabelFormat(LabelFormat.Unit);
				break;
			case Unit:
				setLabelFormat(LabelFormat.Percentage);
				break;
			}
		}
	}
}
