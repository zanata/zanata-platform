package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WebTransEditorView extends DecoratorPanel implements WebTransEditorPresenter.Display {
	
	private final WebTransEditorMenubar header;
	private final WebTransEditorMenubar footer;
	private final Label statusLabel;
	
	private Widget editor;
	private Widget toolbox;
	
	private final VerticalPanel mainPanel;
	private final FlowPanel editorPanel;
	
	public WebTransEditorView() {
		addStyleName("WebTransEditor-Outer");
		this.header = new WebTransEditorMenubar();
		this.footer = new WebTransEditorMenubar();
	
		mainPanel = new VerticalPanel();
		mainPanel.addStyleName("WebTransEditor");
		mainPanel.add(header);
		mainPanel.setCellHeight(header, "20px");

		editorPanel = new FlowPanel();
		editorPanel.setStyleName("WebTransEditor-Editor");
		mainPanel.add(editorPanel);
		editor = new Label("editor");
		editorPanel.add(editor);
		//toolbox = new Label("toolbox");
		//editorPanel.add(toolbox);
		editorPanel.setSize("100%", "100%");
		
		mainPanel.add(footer);
		mainPanel.setCellHeight(footer, "20px");
		
		setWidth("100%");
		setHeight("100%");
		setWidget(mainPanel);
	
		mainPanel.setSize("100%", "100%");
		
		statusLabel = new Label();
		footer.setLeftWidget(statusLabel);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}
	
	@Override
	public void setEditor(Widget editor) {
		this.editor = editor;
		editorPanel.remove(0);
		editorPanel.insert(editor, 0);
		editor.setSize("100%", "80%");
	}

	@Override
	public HasThreeColWidgets getHeader() {
		return header;
	}
	
	@Override
	public HasThreeColWidgets getFooter() {
		return footer;
	}
	
	@Override
	public void setStatus(String status) {
		statusLabel.setText(status);
	}

	@Override
	public void setToolBox(Widget toolbox) {
		this.toolbox = toolbox;
		editorPanel.remove(1);
		editorPanel.insert(toolbox, 1);
		toolbox.setSize("100%", "20%");
	}
}
