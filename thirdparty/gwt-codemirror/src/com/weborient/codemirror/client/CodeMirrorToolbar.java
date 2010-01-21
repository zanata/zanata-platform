package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author samangiahi
 *
 */
public class CodeMirrorToolbar extends HorizontalPanel implements Constants{
	
	CodeMirrorEditorWidget widget;
	
	public CodeMirrorToolbar(CodeMirrorEditorWidget widget) {
		this.widget = widget;
		setSize("70px", "30");
		prepareButtons();
		prepareCodeTemplateListBox();
		
	}

	private void prepareButtons() {
		Image undoButton = new Image(TOOLBAR_UNDO_BUTTON_ICON);
		undoButton.addClickListener(new ClickListener() {

			public void onClick(Widget w) {
				if(widget.getText() != null && !widget.getText().equals("")) {
					
					GWT.log("clicked. test: " + widget.getText(), null);
					widget.getCodeMirrorJSNI().undoEditor();
				}
			}
		});

		prepareUndoButtonHover(undoButton);
		add(undoButton);

		Image redoButton = new Image(TOOLBAR_REDO_BUTTON_ICON);
		redoButton.addClickListener(new ClickListener() {

			public void onClick(Widget w) {
				widget.getCodeMirrorJSNI().redoEditor();
			}
		});

		prepareRedoButtonHover(redoButton);
		add(redoButton);

		Image refreshButton = new Image(TOOLBAR_REFRESH_BUTTON_ICON);
		refreshButton.addClickListener(new ClickListener() {

			public void onClick(Widget w) {
				widget.getCodeMirrorJSNI().reindentEditor();
			}
		});

		prepareRefreshButtonHover(refreshButton);
		add(refreshButton);

	}

	private void prepareCodeTemplateListBox() {
		if (widget.getConfiguration() != null
				&& widget.getConfiguration() != null && widget.getConfiguration().getListBoxPreSets() != null
				&& widget.getConfiguration().getListBoxPreSets().length > 0) {
			
			HorizontalPanel templateSelectorPanel = new HorizontalPanel();
			
			templateSelectorPanel.setStyleName("templateSelectorPanel");
			
			Label label = new Label(widget.getConfiguration().getTagSelectorLabel());
			label.setStyleName("template-label");
			templateSelectorPanel.add(label);
			
			CodeMirrorListBox listBox = new CodeMirrorListBox(widget);
			templateSelectorPanel.add(listBox);
			
			add(templateSelectorPanel);
		}
	}
	
	private void prepareRefreshButtonHover(final Image redoButton) {
		redoButton.addMouseListener(new AbstractIconMouseListener() {

			public void onMouseOver(Widget w) {
				redoButton.setUrl(REFRESH_BUTTON_HOVER_ICON);
			}

			public void onMouseOut(Widget w) {
				redoButton.setUrl(TOOLBAR_REFRESH_BUTTON_ICON);
			}

		});
	}

	private void prepareRedoButtonHover(final Image redoButton) {
		redoButton.addMouseListener(new AbstractIconMouseListener() {

			public void onMouseOver(Widget w) {
				redoButton.setUrl(REDO_BUTTON_HOVER_ICON);
			}

			public void onMouseOut(Widget w) {
				redoButton.setUrl(TOOLBAR_REDO_BUTTON_ICON);
			}
		});
	}

	private void prepareUndoButtonHover(final Image undoButton) {
		undoButton.addMouseListener(new AbstractIconMouseListener() {

			public void onMouseOver(Widget w) {
				undoButton.setUrl(UNDO_BUTTON_HOVER_ICON);
			}

			public void onMouseOut(Widget w) {
				undoButton.setUrl(TOOLBAR_UNDO_BUTTON_ICON);
			}
		});
	}
}
