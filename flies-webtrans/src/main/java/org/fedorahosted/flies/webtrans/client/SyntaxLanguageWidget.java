package org.fedorahosted.flies.webtrans.client;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.weborient.codemirror.client.SyntaxLanguage;
import com.weborient.codemirror.client.SyntaxObservable;
import com.weborient.codemirror.client.SyntaxSelection;

public class SyntaxLanguageWidget extends Composite implements SyntaxObservable {
	Collection<SyntaxSelection> observers = new HashSet<SyntaxSelection>();
	private SyntaxLanguage syntax; 
	public SyntaxLanguageWidget() {
		this(SyntaxLanguage.NONE);
	}

	public SyntaxLanguageWidget(SyntaxLanguage syntax) {
		this.syntax = syntax;
		DisclosurePanel discPanel = new DisclosurePanel("Highlighting");
		FlowPanel panel = new FlowPanel();
		
		panel.add(createButton("None", SyntaxLanguage.NONE));
		panel.add(createButton("JavaScript", SyntaxLanguage.JAVASCRIPT));
		panel.add(createButton("XML", SyntaxLanguage.XML));
		panel.add(createButton("HTML+", SyntaxLanguage.MIXED));
		
		discPanel.add(panel);
		initWidget(discPanel);
	}

	private RadioButton createButton(String name, final SyntaxLanguage buttonSyntax) {
		RadioButton radioButton = new RadioButton("syntax"+System.identityHashCode(this), name);
		radioButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setSyntax(buttonSyntax);
			}
		});
		if(syntax == buttonSyntax)
			radioButton.setValue(true, false);
		return radioButton;
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
