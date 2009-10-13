package org.fedorahosted.flies.webtrans.editor;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WebTransEditorView extends VerticalPanel implements WebTransEditorPresenter.Display {

	@Inject
	public WebTransEditorView(WebTransEditorHeader header, WebTransEditorFooter footer, WebTransScrollTable table) {
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
	

}
