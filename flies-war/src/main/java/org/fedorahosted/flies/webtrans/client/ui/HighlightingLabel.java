package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class HighlightingLabel extends Label {

	private String plainText;
	
	public HighlightingLabel() {
		addStyleName("prettyprint lang-xml");
	}	
	
	public HighlightingLabel(String text) {
		super(text);
	}

	@Override
	public String getText() {
		return plainText;
	}
	
	@Override
	public void setText(String text) {
		super.setText(text);
		highlight();
	}
	
	private void highlight() {
		Element element = getElement();
		Prettify.doHighlight(element);
	}

}

