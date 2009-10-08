package com.weborient.codemirror.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;


public class HiddenSyntaxValue implements HasValue<ParserSyntax> {
	private HandlerManager handlerManager;

	private ParserSyntax value;
	
	public HiddenSyntaxValue() {
		this(ParserSyntax.NONE);
	}
	
	public HiddenSyntaxValue(ParserSyntax value) {
		this.value = value;
	}
	
	private HandlerManager getHandlerManager() {
		return handlerManager == null ? 
				handlerManager = new HandlerManager(this) : handlerManager;
	}

	public ParserSyntax getValue() {
		return value;
	}

	public void setValue(ParserSyntax value) {
		setValue(value, false);
	}

	public void setValue(ParserSyntax value, boolean fireEvents) {
		ParserSyntax oldValue = this.value;
		this.value = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}
	
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<ParserSyntax> handler) {
		return getHandlerManager().addHandler(ValueChangeEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		if (handlerManager != null)
			handlerManager.fireEvent(event);
	}

}
