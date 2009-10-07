package org.fedorahosted.flies.webtrans.client.mvp;

import com.google.gwt.gen2.table.client.InlineCellEditor;
import com.weborient.codemirror.client.CodeMirrorConfiguration;
import com.weborient.codemirror.client.CodeMirrorEditorWidget;
import com.weborient.codemirror.client.SyntaxLanguage;

public class HighlightingCellEditor extends InlineCellEditor<String> {

	private final CodeMirrorEditorWidget editor;
	
	public HighlightingCellEditor() {
		this(createWidget());
		editor.setSyntax(SyntaxLanguage.MIXED);
	}
	
	public HighlightingCellEditor(CodeMirrorEditorWidget widget) {
		super(widget);
		this.editor = widget;
	}
	
	@Override
	public void editCell(
			CellEditInfo cellEditInfo,
			String cellValue,
			Callback<String> callback) {
		super.editCell(cellEditInfo, cellValue, callback);
		editor.getTextArea().setFocus(true);
	}
	
	@Override
	protected String getValue() {
		return editor.getText();
	}

	@Override
	protected void setValue(String cellValue) {
		editor.setText(cellValue);
	}
	
	public static CodeMirrorEditorWidget createWidget() {
        // use the configuration class in order 
        // to override the default widget configuraions

        CodeMirrorConfiguration configuration = new CodeMirrorConfiguration();
        configuration.setLineNumbers(false);
        configuration.setTextWrapping(true);
        configuration.setToolbar(false);
   
        // pass the configuration object to the widget

        return new CodeMirrorEditorWidget(configuration);
	}

}
