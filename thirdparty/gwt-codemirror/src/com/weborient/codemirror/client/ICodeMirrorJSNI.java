package com.weborient.codemirror.client;

public interface ICodeMirrorJSNI {

	String getEditorCode();
	ParserSyntax getSyntax();
	void setSyntax(ParserSyntax syntax);
	void redoEditor();
	void reindentEditor();
	void replaceText(String text);
	void setEditorCode(String code);
	void undoEditor();
}
