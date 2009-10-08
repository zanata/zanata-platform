package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.weborient.codemirror.client.SyntaxLanguage;

public class SyntaxToggleWidget extends Composite implements HasValue<SyntaxLanguage> {
	private SyntaxLanguage syntax, syntaxWhenOn; 
	private CheckBox checkbox = new CheckBox(); 

	public SyntaxToggleWidget(String label, final SyntaxLanguage syntaxWhenOn, boolean initialValue) {
		this.syntax = initialValue ? syntaxWhenOn : SyntaxLanguage.NONE;
		this.syntaxWhenOn = syntaxWhenOn;
		checkbox = new CheckBox(label);
		checkbox.setValue(initialValue);

		checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue())
					setValue(syntaxWhenOn, true);
				else
					setValue(SyntaxLanguage.NONE, true);
			}
		});
		initWidget(checkbox);
	}
	
	@Override
	public SyntaxLanguage getValue() {
		return syntax;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<SyntaxLanguage> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setValue(SyntaxLanguage value) {
		setValue(value, false);
	}

	@Override
	public void setValue(SyntaxLanguage value, boolean fireEvents) {
		assert(value == SyntaxLanguage.NONE || value == syntaxWhenOn);
		SyntaxLanguage oldValue = this.syntax;
		this.syntax = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

}
