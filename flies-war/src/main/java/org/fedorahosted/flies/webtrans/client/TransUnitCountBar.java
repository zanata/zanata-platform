package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.webtrans.client.editor.HasTransUnitCount;

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

public class TransUnitCountBar extends Composite implements HasTransUnitCount {

	private static TransUnitCountBarUiBinder uiBinder = GWT
			.create(TransUnitCountBarUiBinder.class);

	interface TransUnitCountBarUiBinder extends UiBinder<Widget, TransUnitCountBar> {
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

	private final TransUnitCount count = new TransUnitCount(); 
	
	private final WebTransMessages messages;
	
	@Inject
	public TransUnitCountBar(WebTransMessages messages) {
		this.messages = messages;
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void refresh() {
		int approved = count.get(ContentState.Approved);
		int needReview = count.get(ContentState.NeedReview);
		int untranslated = count.get(ContentState.New);
		int total = approved + needReview + untranslated;
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
			int completePx = approved * 100 / total * width /100;
			int inProgressPx = needReview * 100 / total * width /100;;
			int unfinishedPx = untranslated * 100 / total * width /100;;
			
			layoutPanel.setWidgetLeftWidth(undefinedPanel, 0.0, Unit.PX, 0, Unit.PX);
			
			layoutPanel.setWidgetLeftWidth(approvedPanel, 0.0, Unit.PX, completePx, Unit.PX);
			layoutPanel.setWidgetLeftWidth(needReviewPanel, completePx, Unit.PX, inProgressPx, Unit.PX);
			layoutPanel.setWidgetLeftWidth(untranslatedPanel, completePx+inProgressPx, Unit.PX, unfinishedPx, Unit.PX);

			switch(labelFormat) {
			case Percentage:
				label.setText( messages.statusBarLabelPercentage(approved*100/total, needReview*100/total, untranslated*100/total));
				break;
			case Unit:
				label.setText( messages.statusBarLabelUnits(approved, needReview, untranslated));
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
	
	@Override
	public void setToggleEnabled(boolean toggleEnabled) {
		this.toggleEnabled = toggleEnabled;
	}
	
	@Override
	public boolean isToggleEnabled() {
		return toggleEnabled;
	}
	
	@Override
	public void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
		label.setVisible(labelVisible);
	}
	
	@Override
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
	
	@Override
	public int getTotal() {
		return count.getTotal();
	}

	@Override
	public int getCount(ContentState state) {
		return count.get(state);
	}
	
	@Override
	public void setCount(ContentState state, int count) {
		this.count.set(state, count);
		refresh();
	}
	
	@Override
	public void setCount(int approved, int needReview, int untranslated) {
		count.set(ContentState.Approved, approved);
		count.set(ContentState.NeedReview, needReview);
		count.set(ContentState.New, untranslated);
		refresh();
	}
	
	@Override
	public void setCount(TransUnitCount count) {
		this.count.set(count);
		refresh();
	}
}
