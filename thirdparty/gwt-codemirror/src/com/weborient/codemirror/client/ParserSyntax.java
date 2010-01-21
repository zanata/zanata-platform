package com.weborient.codemirror.client;

public enum ParserSyntax {
	JAVASCRIPT("JavaScript", "JSParser"),
	MIXED("HTML", "HTMLMixedParser"),
	NONE("Plain text", "DummyParser"),
	XML("XML", "XMLParser");

	private final String shortDesc;
	private final String parserName;
	private ParserSyntax(String shortDesc, String parserName) {
		this.shortDesc = shortDesc;
		this.parserName = parserName;
	}
	
	String getParserName() {
		return parserName;
	}
	
	public String getShortDesc() {
		return shortDesc;
	}
}
