package com.weborient.codemirror.client;

public enum SyntaxLanguage {
	JAVASCRIPT("JSParser"),
	MIXED("HTMLMixedParser"),
	NONE("DummyParser"),
	XML("XMLParser");

	private final String parserName;
	private SyntaxLanguage(String parserName) {
		this.parserName = parserName;
	}
	
	String getParserName() {
		return parserName;
	}
}
