package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SouthView extends Composite implements SouthPresenter.Display, HasValueChangeHandlers<Boolean>{
	DisclosurePanel disclosurePanel = new DisclosurePanel("Translation Memory Tools");
	TabPanel tabPanel = new TabPanel();
	FlowPanel transPanel = new FlowPanel();
	TextArea glossary = new TextArea();
	
	public SouthView() {
		disclosurePanel.setWidth("100%");
		disclosurePanel.setOpen(false);
		tabPanel.add(transPanel, "Translation Memory");
		glossary.setText("glossary............................................................\nglossary\nglossary");
		tabPanel.add(glossary, "Glossary");
		disclosurePanel.add(tabPanel);
		tabPanel.setWidth("100%");
		//int glossIndex = tabPanel.getWidgetIndex(glossary);
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if(disclosurePanel.isOpen()) {
					if (event.getSelectedItem() == tabPanel.getWidgetIndex(transPanel)) {
						ValueChangeEvent.fire(SouthView.this, true);
					}else {
						ValueChangeEvent.fire(SouthView.this, false);
					}
				} else {
					ValueChangeEvent.fire(SouthView.this, false);
				}
			}
		});
		//transPanel is set default
		tabPanel.selectTab(tabPanel.getWidgetIndex(transPanel));
	}

	@Override
	public Widget asWidget() {
		return disclosurePanel;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}

	@Override
	public HasText getGlossary() {
		return glossary;
	}

	@Override
	public HasWidgets getWidgets() {
		return transPanel;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public HasValueChangeHandlers<Boolean> getValueChangeHandlers() {
		return this;
	}
}
