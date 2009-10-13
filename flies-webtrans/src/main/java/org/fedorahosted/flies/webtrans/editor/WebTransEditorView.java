package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WebTransEditorView extends VerticalPanel implements WebTransEditorPresenter.Display {
	
	private final WebTransEditorHeader header;
	private final WebTransEditorFooter footer;
	private final WebTransScrollTable scrollTable;
	@Inject
	public WebTransEditorView(WebTransEditorHeader header, WebTransEditorFooter footer, WebTransScrollTable table) {
		this.header = header;
		this.footer = footer;
		this.scrollTable = table;
		
		setSize("100%", "100%");
		
		add(header);
		setCellHeight(header, "20px");
		
		add(table);
		
		add(footer);
		setCellHeight(footer, "20px");

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
	
	// will refactor when we have a presenter for header..
	@Override
	public Pager getPager() {
		return footer.getPager();
	}

	@Override
	public WebTransScrollTable getScrollTable() {
		return scrollTable;
	}
}
