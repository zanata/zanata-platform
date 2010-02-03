package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SouthView implements SouthPresenter.Display {
	DisclosurePanel disclosurePanel = new DisclosurePanel("Translation Memory Tools", false);
	TabPanel tabPanel = new TabPanel();
	TextArea glossary = new TextArea();
	TextArea related = new TextArea();
	public SouthView() {
		disclosurePanel.setWidth("100%");
		glossary.setText("glossary............................................................\nglossary\nglossary");
		tabPanel.add(glossary, "Glossary");
		related.setText("related\nrelated................................................................\nrelated");
		tabPanel.add(related, "Related");
		disclosurePanel.add(tabPanel);
		tabPanel.setWidth("100%");
		tabPanel.selectTab(0);
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

}
