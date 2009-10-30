package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.gwt.model.TransUnit;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class SourcePanel extends Composite implements HasValue<TransUnit>{

	private final FlowPanel panel;
	private final Label sourceLabel;
	private final Label fuzzyLabel;
	private TransUnit value;
	
	public SourcePanel(TransUnit value) {
		this.value = value;
		panel = new FlowPanel();
		panel.setSize("100%", "100%");

		initWidget(panel);

		sourceLabel = new HighlightingLabel(value.getSource(), ParserSyntax.MIXED);
		sourceLabel.setStylePrimaryName("webtrans-editor-content");
		sourceLabel.addStyleName("webtrans-editor-content-source");

		VerticalPanel vPanel = new VerticalPanel();
		vPanel.setStylePrimaryName("webtrans-editor-status");
		fuzzyLabel = new Label("F");
		vPanel.add(fuzzyLabel);
		vPanel.setWidth("10px");
		
		panel.add(vPanel);

		panel.add(sourceLabel);
		refresh();
	}
	
	public void refresh() {
		fuzzyLabel.setVisible(value.isFuzzy());
	}
	
	@Override
	public TransUnit getValue() {
		return value;
	}
	@Override
	public void setValue(TransUnit value) {
		setValue(value, true);
	}
	
	@Override
	public void setValue(TransUnit value, boolean fireEvents) {
		if(this.value != value) {
			this.value = value;
			if(fireEvents) {
				ValueChangeEvent.fire(this, value);
			}
			refresh();
		}
	}
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<TransUnit> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	
	

}
