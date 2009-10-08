package org.fedorahosted.flies.webtrans.client;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.weborient.codemirror.client.SyntaxLanguage;
import com.weborient.codemirror.client.SyntaxObservable;
import com.weborient.codemirror.client.SyntaxSelection;

public class SyntaxLanguageWidget extends Composite implements SyntaxObservable {
	Collection<SyntaxSelection> observers = new HashSet<SyntaxSelection>();
	private SyntaxLanguage syntax; 
	ListBox listBox = new ListBox();
	public SyntaxLanguageWidget() {
		this(SyntaxLanguage.NONE);
	}

	public SyntaxLanguageWidget(SyntaxLanguage syntax) {
		this.syntax = syntax;

		Panel panel = new HorizontalPanel();
		panel.add(new Label("Highlighting: "));
		addItem("Plain text", SyntaxLanguage.NONE);
		addItem("JavaScript", SyntaxLanguage.JAVASCRIPT);
		addItem("XML", SyntaxLanguage.XML);
		addItem("HTML+", SyntaxLanguage.MIXED);
		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setSyntax(SyntaxLanguage.valueOf(listBox.getValue(listBox.getSelectedIndex())));
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
	
	public void setSyntax(SyntaxLanguage syntax) {
		this.syntax = syntax;
		for (SyntaxSelection observer : observers) {
			observer.setSyntax(syntax);
		}
	}
	
	@Override
	public SyntaxLanguage getSyntax() {
		return syntax;
	}
	
	@Override
	public void addObserver(SyntaxSelection observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(SyntaxSelection observer) {
		observers.remove(observer);
	}

}
