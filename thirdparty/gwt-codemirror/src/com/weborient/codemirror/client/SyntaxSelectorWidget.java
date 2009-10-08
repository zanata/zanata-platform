package com.weborient.codemirror.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;

public class SyntaxSelectorWidget extends Composite implements HasValue<ParserSyntax> {
	private ParserSyntax syntax; 
	ListBox listBox = new ListBox();
	public SyntaxSelectorWidget() {
		this(ParserSyntax.NONE);
	}

	public SyntaxSelectorWidget(ParserSyntax initialSyntax) {
		this.syntax = initialSyntax;

		Panel panel = new HorizontalPanel();
		panel.add(new Label("Highlighting: "));
		addItem(ParserSyntax.NONE);
		addItem(ParserSyntax.JAVASCRIPT);
		addItem(ParserSyntax.XML);
		addItem(ParserSyntax.MIXED);
		listBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String syntaxName = listBox.getValue(listBox.getSelectedIndex());
				ParserSyntax syntax = ParserSyntax.valueOf(syntaxName);
				setValue(syntax, true);
			}
		});
		panel.add(listBox);
		initWidget(panel);
	}

	private void addItem(final ParserSyntax buttonSyntax) {
		listBox.addItem(buttonSyntax.getShortDesc(), buttonSyntax.name());
		if (syntax == buttonSyntax)
			listBox.setSelectedIndex(listBox.getItemCount()-1);
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
		ParserSyntax oldValue = this.syntax;
		this.syntax = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

}
