package com.weborient.codemirror.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author samangiahi
 *
 */
public class CodeMirrorEditorWidget extends Composite implements Constants, SyntaxSelection {
	
	TextArea textArea;
	CodeMirrorConfiguration configuration;
	ICodeMirrorJSNI codeMirrorJSNI = new DummyCodeMirrorJSNI();
	HorizontalPanel toolbar;

	public CodeMirrorEditorWidget() {
		this(new CodeMirrorConfiguration());
	}

	public CodeMirrorEditorWidget(CodeMirrorConfiguration configuration) {
		super();
		this.configuration = configuration;
		initWidget();
	}

	private void initWidget() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth(configuration.getWidth());
		panel.setStyleName("codemirror-ed");
		textArea = new TextArea();
		DOM.setElementAttribute(textArea.getElement(), "id", configuration.getId());
		panel.add(getToolbar());
		panel.add(textArea);
		initWidget(panel);
	}

	private HorizontalPanel getToolbar() {
		toolbar = new HorizontalPanel();
		toolbar.setStyleName("html-editor-toolbar");
		toolbar.setWidth(TOOLBAR_WIDTH);
		toolbar.setHeight(TOOLBAR_WIDTH);
		
		return toolbar;
	}



	protected void onLoad() {
		String text = codeMirrorJSNI.getEditorCode();
		SyntaxLanguage syntax = codeMirrorJSNI.getSyntax();
		if (configuration != null) {
			codeMirrorJSNI = new CodeMirrorJSNI(configuration, text, syntax);
		} else {
			codeMirrorJSNI = new CodeMirrorJSNI(new CodeMirrorConfiguration(), text, syntax);
		}
		if (configuration == null || configuration.isToolbar())
			toolbar.add(new CodeMirrorToolbar(this));
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(TextArea ta) {
		this.textArea = ta;
	}

	public String getText() {
		return codeMirrorJSNI.getEditorCode();
	}

	public void setText(String code) {
		codeMirrorJSNI.setEditorCode(code);
	}

	public HorizontalPanel getToolBar() {
		return toolbar;
	}

	public void replaceText(String text) {
		codeMirrorJSNI.replaceText(text);
	}

	public CodeMirrorConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(CodeMirrorConfiguration configuration) {
		this.configuration = configuration;
	}

	public ICodeMirrorJSNI getCodeMirrorJSNI() {
		return codeMirrorJSNI;
	}

	public void setCodeMirrorJSNI(ICodeMirrorJSNI codeMirrorJSNI) {
		this.codeMirrorJSNI = codeMirrorJSNI;
	}

	public void setSyntax(SyntaxLanguage syntax) {
		codeMirrorJSNI.setSyntax(syntax);
	}
	
}
