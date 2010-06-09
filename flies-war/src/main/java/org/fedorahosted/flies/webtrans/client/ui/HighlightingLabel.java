package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class HighlightingLabel extends Label {

	private String plainText;
	
	public HighlightingLabel() {
	}	
	
	public HighlightingLabel(String text) {
		super();
		setText(text);
	}

	@Override
	public String getText() {
		return plainText;
	}
	
	@Override
	public void setText(String text) {
		this.plainText = text;
		super.setText(text);
		highlight();
	}
	
	private void highlight() {
		Element element = getElement();
		String text = plainText == null ? "" : plainText;
		CodeMirror.doHighlight(text, element);
	}

}

