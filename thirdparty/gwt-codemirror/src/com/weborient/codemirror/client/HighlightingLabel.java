package com.weborient.codemirror.client;

import com.google.gwt.dom.client.Node;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class HighlightingLabel extends Label {
	private String plainText;
	private HasValue<ParserSyntax> syntaxValue;
	private HandlerRegistration handlerRegistration;
	
	public HighlightingLabel() {
		this("", new HiddenSyntaxValue(ParserSyntax.NONE));
	}	
	
	public HighlightingLabel(String text) {
		this(text, new HiddenSyntaxValue(ParserSyntax.NONE));
	}
	
	public HighlightingLabel(String text, ParserSyntax syntax) {
		this(text, new HiddenSyntaxValue(syntax));
	}

	public HighlightingLabel(String text, HasValue<ParserSyntax> syntaxValue) {
		plainText = text;
		this.syntaxValue = syntaxValue;
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
			ParserSyntax syntax = syntaxValue.getValue();
			if (syntax == ParserSyntax.NONE) {
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

	
	@Override
	protected void onLoad() {
		super.onLoad();
		handlerRegistration = syntaxValue.addValueChangeHandler(new ValueChangeHandler<ParserSyntax>() {
			public void onValueChange(ValueChangeEvent<ParserSyntax> event) {
				highlight();
			}
		});
		highlight();
	}
	
	@Override
	protected void onUnload() {
		handlerRegistration.removeHandler();
		handlerRegistration = null;
		super.onUnload();
	}

}

