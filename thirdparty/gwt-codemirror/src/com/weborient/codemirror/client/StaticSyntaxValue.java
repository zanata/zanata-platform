package com.weborient.codemirror.client;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;


public class StaticSyntaxValue implements HasValue<SyntaxLanguage> {

	private final SyntaxLanguage value;
	
	public StaticSyntaxValue() {
		this(SyntaxLanguage.NONE);
	}
	
	public StaticSyntaxValue(SyntaxLanguage value) {
		this.value = value;
	}

	public SyntaxLanguage getValue() {
		return value;
	}

	public void setValue(SyntaxLanguage value) {
		setValue(value, false);
	}

	public void setValue(SyntaxLanguage value, boolean fireEvents) {
		throw new RuntimeException("setValue() not supported by StaticSyntaxValue");
	}
	
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<SyntaxLanguage> handler) {
		return new DummyHandlerRegistration();
	}

	public void fireEvent(GwtEvent<?> event) {
		// NB static implementation never fires events
	}

}
