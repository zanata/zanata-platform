package com.weborient.codemirror.client;

public interface SyntaxObservable {

	void addObserver(SyntaxSelection observer);
	void removeObserver(SyntaxSelection observer);
	SyntaxLanguage getSyntax();

}
