package com.weborient.codemirror.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

public class SyntaxToggleWidget extends Composite implements HasValue<ParserSyntax> {
	private ParserSyntax syntax, syntaxWhenOn; 
	private CheckBox checkbox = new CheckBox(); 

	public SyntaxToggleWidget(final ParserSyntax syntaxWhenOn, boolean initialValue) {
		this(syntaxWhenOn.getShortDesc(), syntaxWhenOn, initialValue);
	}
	
	public SyntaxToggleWidget(String label, final ParserSyntax syntaxWhenOn, boolean initialValue) {
		this.syntax = initialValue ? syntaxWhenOn : ParserSyntax.NONE;
		this.syntaxWhenOn = syntaxWhenOn;
		checkbox = new CheckBox(label);
		checkbox.setValue(initialValue);

		checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue())
					setValue(syntaxWhenOn, true);
				else
					setValue(ParserSyntax.NONE, true);
			}
		});
		initWidget(checkbox);
	}
	
	public ParserSyntax getValue() {
		return syntax;
	}

	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<ParserSyntax> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public void setValue(ParserSyntax value) {
		setValue(value, false);
	}

	public void setValue(ParserSyntax value, boolean fireEvents) {
		assert(value == ParserSyntax.NONE || value == syntaxWhenOn);
		ParserSyntax oldValue = this.syntax;
		this.syntax = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

}
