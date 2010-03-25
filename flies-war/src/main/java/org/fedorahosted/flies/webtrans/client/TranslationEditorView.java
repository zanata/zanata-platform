package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.ui.HasPager;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationEditorView extends Composite implements TranslationEditorPresenter.Display {

	private static TranslationEditorViewUiBinder uiBinder = GWT
			.create(TranslationEditorViewUiBinder.class);

	interface TranslationEditorViewUiBinder extends
			UiBinder<Widget, TranslationEditorView> {
	}

	@UiField
	FlowPanel tmPanel, transUnitNavigationContainer;

	@UiField
	LayoutPanel editor;
	
	@UiField
	SplitLayoutPanel splitPanel;
	
	@UiField(provided=true)
	TransUnitCountBar transUnitCountBar;
	
	@UiField(provided=true)
	Pager pager;
	
	@Inject
	public TranslationEditorView(final WebTransMessages messages, final Resources resources) {
		this.transUnitCountBar = new TransUnitCountBar(messages);
		this.pager = new Pager(messages, resources);
		initWidget(uiBinder.createAndBindUi(this));
		splitPanel.setWidgetMinSize(tmPanel, 150);
	}

	@Override
	public void setTranslationMemoryView(Widget translationMemoryView) {
		tmPanel.clear();
		tmPanel.add(translationMemoryView);
	}

	@Override
	public void setEditorView(Widget editor) {
		this.editor.clear();
		this.editor.add(editor);
		
	}
	
	@Override
	public void setTransUnitNavigation(Widget navigationWidget) {
		transUnitNavigationContainer.clear();
		transUnitNavigationContainer.add(navigationWidget);
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
	public HasTransUnitCount getTransUnitCount() {
		return transUnitCountBar;
	}
	
	@Override
	public HasPager getPageNavigation() {
		return pager;
	}
	
}
