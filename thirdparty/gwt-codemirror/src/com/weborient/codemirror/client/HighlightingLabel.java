package com.weborient.codemirror.client;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class HighlightingLabel extends Label implements SyntaxSelection {
	private String plainText;
	private SyntaxLanguage syntax;
	private SyntaxObservable observable;
	
	public HighlightingLabel() {
		this("");
	}	
	
	public HighlightingLabel(String text) {
		super();
		plainText = text;
		setSyntax(SyntaxLanguage.NONE);
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
		if (isAttached()) {
			if (syntax == SyntaxLanguage.NONE) {
				super.setText(plainText);
			} else {
				doHighlight(plainText, element, syntax.getParserName());
			}
		}
	}

	private native void doHighlight(String text, Element node, String parserName)/*-{
		var parser = eval("$wnd."+parserName);
		$wnd.highlightText(text, node, parser);
	}-*/;

	public void setSyntax(SyntaxLanguage syntax) {
		this.syntax = syntax;
		highlight();
	}
	
	public void observe(SyntaxObservable observable) {
		observable.addObserver(this);
		this.observable = observable;
		setSyntax(observable.getSyntax());
	}
	
	@Override
	protected void onLoad() {
		highlight();
	}
	
	@Override
	protected void onUnload() {
		observable.removeObserver(this);
	}
}

