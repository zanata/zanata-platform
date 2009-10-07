package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class HighlightingLabel extends Label implements LangSupport {
	private String plainText;
	@SuppressWarnings("unused") // used in JS
	private String parserName;
	
	public HighlightingLabel() {
		this("");
	}	
	
	public HighlightingLabel(String text) {
		plainText = text;
		setLanguage(SyntaxLanguage.NONE);
	}
	
	@Override
	public String getText() {
		return plainText;
	}
	
	@Override
	public void setText(String text) {
		plainText = text;
		highlight();
	}
	
	private void highlight() {
//		GWT.log("highlight()", null);
		Element element = getElement();
		Node child;
		while ((child = element.getFirstChild()) != null)
			element.removeChild(child);
		if (isAttached())
			doHighlight();
	}

	private native void doHighlight()/*-{
		var text = this.@com.weborient.codemirror.client.HighlightingLabel::plainText;
		var node = this.@com.weborient.codemirror.client.HighlightingLabel::getElement()();
		var parserName = this.@com.weborient.codemirror.client.HighlightingLabel::parserName;
		var parser = eval("$wnd."+parserName);
		$wnd.highlightText(text, node, parser);
	}-*/;

	public void setLanguage(SyntaxLanguage lang) {
		parserName = lang.getParserName();
		highlight();
	}
	
	@Override
	protected void onLoad() {
		highlight();
	}
}

