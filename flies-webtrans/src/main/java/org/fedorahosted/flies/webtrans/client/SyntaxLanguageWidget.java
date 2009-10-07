package org.fedorahosted.flies.webtrans.client;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
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
		Panel syntaxBar = new HorizontalPanel();
		syntaxBar.add(new Label("Highlighting: "));
		String groupName = "syntaxBar"+System.identityHashCode(this);
		RadioButton noneButton = new RadioButton(groupName, "None");
		noneButton.setValue(true, false);
		syntaxBar.add(noneButton);
		RadioButton jsButton = new RadioButton(groupName, "JavaScript");
		syntaxBar.add(jsButton);
		RadioButton xmlButton = new RadioButton(groupName, "XML");
		syntaxBar.add(xmlButton);
		RadioButton mixedButton = new RadioButton(groupName, "HTML");
		syntaxBar.add(mixedButton);
		
		switch(syntax) {
		case JAVASCRIPT:
			jsButton.setValue(true, false);
			break;
		case MIXED:
			mixedButton.setValue(true, false);
			break;
		case NONE:
			noneButton.setValue(true, false);
			break;
		case XML:
			xmlButton.setValue(true, false);
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		noneButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				setSyntax(SyntaxLanguage.NONE);
			}});
		jsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				setSyntax(SyntaxLanguage.JAVASCRIPT);
			}});
		xmlButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				setSyntax(SyntaxLanguage.XML);
			}});
		mixedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				setSyntax(SyntaxLanguage.MIXED);
			}});
		
		initWidget(syntaxBar);
	}
	
	void setSyntax(SyntaxLanguage syntax) {
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
