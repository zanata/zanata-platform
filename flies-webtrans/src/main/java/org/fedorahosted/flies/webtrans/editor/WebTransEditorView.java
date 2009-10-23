package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WebTransEditorView extends DecoratorPanel implements WebTransEditorPresenter.Display {
	
	private final WebTransEditorHeader header;
	private final WebTransEditorFooter footer;
	private final WebTransScrollTable scrollTable;

	@Inject
	public WebTransEditorView(WebTransEditorHeader header, WebTransEditorFooter footer, WebTransScrollTable table) {
		addStyleName("TransPanel-Outer");
		this.header = header;
		this.footer = footer;
		this.scrollTable = table;
	
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.addStyleName("TransPanel");
		verticalPanel.add(header);
		verticalPanel.setCellHeight(header, "20px");
		
		verticalPanel.add(table);
		
		verticalPanel.add(footer);
		verticalPanel.setCellHeight(footer, "20px");
		
		setWidth("100%");
		setHeight("100%");
		setWidget(verticalPanel);
	
		verticalPanel.setSize("100%", "100%");
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
	
	@Override
	public CachedWebTransTableModel getCachedTableModel() {
		return scrollTable.getCachedTableModel();
	}
	
}
