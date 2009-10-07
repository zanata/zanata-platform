package org.fedorahosted.flies.webtrans.client;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
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
		
//	    MenuBar menu = new MenuBar();
//	    menu.setAutoOpen(true);
//	    menu.setWidth("500px");
//	    menu.setAnimationEnabled(true);
//	    MenuBar syntaxMenu = new MenuBar(true);
//	    menu.addItem(new MenuItem("Highlighting", syntaxMenu));
		MenuBar syntaxMenu = new MenuBar();
	    
		syntaxMenu.addItem(new MenuItem("None", new Command() {
			@Override
			public void execute() {
				setSyntax(SyntaxLanguage.NONE);
			}
		}));
		syntaxMenu.addItem(new MenuItem("JavaScript", new Command() {
			@Override
			public void execute() {
				setSyntax(SyntaxLanguage.JAVASCRIPT);
			}
		}));
		syntaxMenu.addItem(new MenuItem("XML", new Command() {
			@Override
			public void execute() {
				setSyntax(SyntaxLanguage.XML);
			}
		}));
		syntaxMenu.addItem(new MenuItem("HTML", new Command() {
			@Override
			public void execute() {
				setSyntax(SyntaxLanguage.MIXED);
			}
		}));
		
//		initWidget(menu);
		initWidget(syntaxMenu);

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
