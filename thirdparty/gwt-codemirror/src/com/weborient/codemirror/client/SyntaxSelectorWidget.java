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

public class SyntaxSelectorWidget extends Composite implements HasValue<SyntaxLanguage> {
	private SyntaxLanguage syntax; 
	ListBox listBox = new ListBox();
	public SyntaxSelectorWidget() {
		this(SyntaxLanguage.NONE);
	}

	public SyntaxSelectorWidget(SyntaxLanguage syntax) {
		this.syntax = syntax;

		Panel panel = new HorizontalPanel();
		panel.add(new Label("Highlighting: "));
		addItem("Plain text", SyntaxLanguage.NONE);
		addItem("JavaScript", SyntaxLanguage.JAVASCRIPT);
		addItem("XML", SyntaxLanguage.XML);
		addItem("HTML+", SyntaxLanguage.MIXED);
		listBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String syntaxName = listBox.getValue(listBox.getSelectedIndex());
				SyntaxLanguage syntax = SyntaxLanguage.valueOf(syntaxName);
				setValue(syntax, true);
			}
		});
		panel.add(listBox);
		initWidget(panel);
	}

	private void addItem(String name, final SyntaxLanguage buttonSyntax) {
		listBox.addItem(name, buttonSyntax.name());
		if (syntax == buttonSyntax)
			listBox.setSelectedIndex(listBox.getItemCount()-1);
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
		SyntaxLanguage oldValue = this.syntax;
		this.syntax = value;
		if (fireEvents)
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
	}

}
