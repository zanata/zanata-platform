package org.fedorahosted.flies.webtrans.client.mvp;

import com.google.gwt.gen2.table.client.InlineCellEditor;
import com.google.gwt.user.client.ui.TextArea;

public class TextAreaCellEditor extends InlineCellEditor<String> {

	private final TextArea textarea;
	
	protected TextAreaCellEditor(TextArea textarea) {
		super(textarea);
		this.textarea = textarea;
	}
	
	public TextAreaCellEditor() {
		this(new TextArea());
	}
	
	@Override
	public void editCell(
			CellEditInfo cellEditInfo,
			String cellValue,
			Callback<String> callback) {
		super.editCell(cellEditInfo, cellValue, callback);
		textarea.setFocus(true);
	}
	
	@Override
	protected String getValue() {
		return textarea.getText();
	}

	@Override
	protected void setValue(String cellValue) {
		textarea.setText(cellValue);
	}

}
