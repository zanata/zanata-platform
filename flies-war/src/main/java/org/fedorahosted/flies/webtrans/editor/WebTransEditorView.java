package org.fedorahosted.flies.webtrans.editor;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class WebTransEditorView extends FlowPanel implements WebTransEditorPresenter.Display {
	
	interface WebTransEditorViewUiBinder extends UiBinder<Widget, WebTransEditorView> {
	}

	private static WebTransEditorViewUiBinder uiBinder = GWT.create(WebTransEditorViewUiBinder.class);
	
	private Widget editor;
	private Widget documentList;
	
	@UiField
	FlowPanel headerPanel;
	
	@UiField
	FlowPanel footerPanel;
	
	public WebTransEditorView() {
		uiBinder.createAndBindUi(this);
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
	}

	@Override
	public void setToolBox(Widget toolbox) {
		Log.error("not implemented");
	}
	
	public void setHeaderVisible(boolean visible) {
		headerPanel.setVisible(visible);
	}
	
	public void showDocumentList() {
	}
	
	public void showTranslationEditor() {
	}
	
}
