package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SouthView extends Composite implements SouthPresenter.Display, HasVisibilityEventHandlers {
	DisclosurePanel disclosurePanel = new DisclosurePanel("Translation Memory Tools");
	TabPanel tabPanel = new TabPanel();
	FlowPanel transPanel = new FlowPanel();
	TextArea glossary = new TextArea();
	TextArea related = new TextArea();
	
	public SouthView() {
		disclosurePanel.setWidth("100%");
		disclosurePanel.setOpen(false);
		tabPanel.add(transPanel, "Translation Memory");
		glossary.setText("glossary............................................................\nglossary\nglossary");
		tabPanel.add(glossary, "Glossary");
		related.setText("related\nrelated................................................................\nrelated");
		tabPanel.add(related, "Related");
		disclosurePanel.add(tabPanel);
		tabPanel.setWidth("100%");
		int glossIndex = tabPanel.getWidgetIndex(glossary);
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if(event.getSelectedItem()==tabPanel.getWidgetIndex(transPanel) && disclosurePanel.isOpen()) {
					VisibilityEvent.fire(getVisibilityHandlers(), true);
				}
			}
		});
		tabPanel.selectTab(glossIndex);
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
	public HasText getRelated() {
		return related;
	}

	@Override
	public HasWidgets getWidgets() {
		return transPanel;
	}

	@Override
	public HasVisibilityEventHandlers getVisibilityHandlers() {
		return this;
	}

	@Override
	public HandlerRegistration addVisibilityHandler(VisibilityHandler handler) {
		return addHandler(handler, VisibilityEvent.getType());
	}

}
