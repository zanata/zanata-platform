package com.weborient.codemirror.client;

public interface ICodeMirrorJSNI {

	String getEditorCode();
	SyntaxLanguage getSyntax();
	void setSyntax(SyntaxLanguage syntax);
	void redoEditor();
	void reindentEditor();
	void replaceText(String text);
	void setEditorCode(String code);
	void undoEditor();
}
