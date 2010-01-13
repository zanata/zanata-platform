package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class SouthView extends TabPanel implements SouthPresenter.Display {
	
	TextArea glossary = new TextArea();
	TextArea related = new TextArea();
	public SouthView() {
		glossary.setText("glossary............................................................\nglossary\nglossary");
		add(glossary, "Glossary");
		related.setText("related\nrelated................................................................\nrelated");
		add(related, "Related");
	}

	@Override
	public Widget asWidget() {
		return this;
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
