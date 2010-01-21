package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author samangiahi
 *
 */
public class CodeMirrorListBox extends ListBox implements ChangeListener {
	CodeMirrorEditorWidget widget;

	public CodeMirrorListBox(CodeMirrorEditorWidget widget) {
		this.widget = widget;
		if (widget.getConfiguration() != null
				&& widget.getConfiguration().getListBoxPreSets() != null
				&& widget.getConfiguration().getListBoxPreSets().length > 0) {

			addItem("");
			for (String preCode : widget.getConfiguration()
					.getListBoxPreSets()) {
				addItem(preCode);
			}
			setVisibleItemCount(1);
			addChangeListener(this);
			this.setStyleName("listBox");
		}
	}

	public void onChange(Widget w) {
		String value = getValue(getSelectedIndex());
		GWT.log("list box value: " + value, null);
		if (value != null && !value.equals("")) {
			widget.replaceText(value);
		}
	}
}
