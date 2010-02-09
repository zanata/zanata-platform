package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SouthView implements SouthPresenter.Display {
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
	public HasSelectionHandlers<Integer> getSelectionHandler() {
		return tabPanel;
	}
	
	@Override
	public int getTransPanelIndex() {
		return tabPanel.getWidgetIndex(transPanel);
	}

	@Override
	public boolean isDisclosurePanelOpen() {
		// TODO Auto-generated method stub
		return disclosurePanel.isOpen();
	}
}
