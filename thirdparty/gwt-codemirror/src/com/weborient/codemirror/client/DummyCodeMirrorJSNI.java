package com.weborient.codemirror.client;

public class DummyCodeMirrorJSNI implements ICodeMirrorJSNI {

	private String text;
	private SyntaxLanguage language;
	
	public DummyCodeMirrorJSNI() {
		this(" ");
	}
	
	public DummyCodeMirrorJSNI(String text) {
		this.text = text;
	}
	

	public String getEditorCode() {
		return text;
	}

	public void redoEditor() {
	}
	
	public void reindentEditor() {
	}
	
	public void replaceText(String replace) {
		// Assuming initial selection is an empty string at beginning of text area
		// This shouldn't get called anyway.
		text = replace + text;
	}

	public void setEditorCode(String text) {
		this.text = text;
	}

	public void setLanguage(SyntaxLanguage lang) {
		language = lang;
	}
	
	public void undoEditor() {
	}

}
