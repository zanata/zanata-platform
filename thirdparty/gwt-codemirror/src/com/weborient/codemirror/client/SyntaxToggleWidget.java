package com.weborient.codemirror.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

public class SyntaxToggleWidget extends Composite implements HasValue<SyntaxLanguage> {
	private SyntaxLanguage syntax, syntaxWhenOn; 
	private CheckBox checkbox = new CheckBox(); 

	public SyntaxToggleWidget(String label, final SyntaxLanguage syntaxWhenOn, boolean initialValue) {
		this.syntax = initialValue ? syntaxWhenOn : SyntaxLanguage.NONE;
		this.syntaxWhenOn = syntaxWhenOn;
		checkbox = new CheckBox(label);
		checkbox.setValue(initialValue);

		checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue())
					setValue(syntaxWhenOn, true);
				else
					setValue(SyntaxLanguage.NONE, true);
			}
		});
		initWidget(checkbox);
	}
	
	public SyntaxLanguage getValue() {
		return syntax;
	}

	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<SyntaxLanguage> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public void setValue(SyntaxLanguage value) {
		setValue(value, false);
	}

	public void setValue(SyntaxLanguage value, boolean fireEvents) {
		assert(value == SyntaxLanguage.NONE || value == syntaxWhenOn);
		SyntaxLanguage oldValue = this.syntax;
		this.syntax = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

}
