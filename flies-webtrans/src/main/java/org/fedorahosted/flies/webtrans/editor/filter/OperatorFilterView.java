package org.fedorahosted.flies.webtrans.editor.filter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OperatorFilterView extends VerticalPanel implements OperatorFilterPresenter.Display {
	
	private final VerticalPanel topPanel;
	private final HorizontalPanel bottomPanel;
	private final Button addButton;
	private final ListBox filterTypeBox;
	
	public class FilterWidgetWrapper extends HorizontalPanel {
		private final Widget filter;
		private final Button removeButton;
		
		public FilterWidgetWrapper(Widget filter, ClickHandler removeHandler) {
			this.filter = filter;
			this.removeButton = new Button("-");
			add(filter);
			add(removeButton);
			removeButton.addClickHandler(removeHandler);
		}
	}
	
	public OperatorFilterView() {	
		topPanel = new VerticalPanel();
		bottomPanel = new HorizontalPanel();
		bottomPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		bottomPanel.setSpacing(5);
	
		addButton = new Button("+");
		bottomPanel.add(addButton);	
		
		filterTypeBox = new ListBox();
		filterTypeBox.addItem("Phrase");
		bottomPanel.add(filterTypeBox);
		
		add(topPanel);
		add(bottomPanel);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopProcessing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFilterUnit(Widget widget) {
		final FilterWidgetWrapper filterWrapper = new FilterWidgetWrapper(widget, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			}
		});
		topPanel.add(widget);
	}
	
	@Override
	public void removeFilterUnit(Widget widget) {
		topPanel.remove(widget);
	}
	
	@Override
	public Button getAddButton() {
		return addButton;
	}
}
