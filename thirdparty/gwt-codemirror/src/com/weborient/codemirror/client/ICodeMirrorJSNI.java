package com.weborient.codemirror.client;

public interface ICodeMirrorJSNI extends SyntaxSelection {

	String getEditorCode();
	SyntaxLanguage getSyntax();
	void redoEditor();
	void reindentEditor();
	void replaceText(String text);
	void setEditorCode(String code);
	void undoEditor();
}
