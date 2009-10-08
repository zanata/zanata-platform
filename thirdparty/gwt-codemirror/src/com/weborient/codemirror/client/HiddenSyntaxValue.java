package com.weborient.codemirror.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;


public class HiddenSyntaxValue implements HasValue<SyntaxLanguage> {
	private HandlerManager handlerManager;

	private SyntaxLanguage value;
	
	public HiddenSyntaxValue() {
		this(SyntaxLanguage.NONE);
	}
	
	public HiddenSyntaxValue(SyntaxLanguage value) {
		this.value = value;
	}
	
	private HandlerManager getHandlerManager() {
		return handlerManager == null ? 
				handlerManager = new HandlerManager(this) : handlerManager;
	}

	public SyntaxLanguage getValue() {
		return value;
	}

	public void setValue(SyntaxLanguage value) {
		setValue(value, false);
	}

	public void setValue(SyntaxLanguage value, boolean fireEvents) {
		SyntaxLanguage oldValue = this.value;
		this.value = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}
	
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<SyntaxLanguage> handler) {
		return getHandlerManager().addHandler(ValueChangeEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		if (handlerManager != null)
			handlerManager.fireEvent(event);
	}

}
