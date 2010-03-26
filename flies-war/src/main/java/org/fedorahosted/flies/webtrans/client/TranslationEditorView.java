package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.ui.SplitLayoutPanelHelper;
import org.fedorahosted.flies.webtrans.client.ui.HasPager;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
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

	
	@UiField(provided = true) 
	LayoutPanel tmPanelContainer;
	
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
	
	@UiField
	Image showTmViewLink;
	
	@UiField
	Image tmMinimize;
	
	@UiField(provided=true)
	Resources resources;
	
	private double southHeight = 150;
	
	@Inject
	public TranslationEditorView(final WebTransMessages messages, final Resources resources) {
		this.resources = resources;
		this.transUnitCountBar = new TransUnitCountBar(messages);
		this.pager = new Pager(messages, resources);
		this.tmPanelContainer = new LayoutPanel() {
			public void onBrowserEvent(com.google.gwt.user.client.Event event) {
				
				super.onBrowserEvent(event);
				switch(event.getTypeInt()) {
				case Event.ONMOUSEOVER:
					tmMinimize.setVisible(true);
					break;
				case Event.ONMOUSEOUT:
					tmMinimize.setVisible(false);
					break;
				}
				
			};
		};
		tmPanelContainer.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
			
		initWidget(uiBinder.createAndBindUi(this));
		splitPanel.setWidgetMinSize(tmPanelContainer, 150);
		showTmViewLink.setTitle( messages.showTranslationMemoryPanel() );
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
	
	@Override
	public HasClickHandlers getHideTMViewButton() {
		return tmMinimize;
	}

	@Override
	public void setTmViewVisible(boolean visible) {
		splitPanel.forceLayout();
		if(visible) {
			SplitLayoutPanelHelper.setSplitPosition(splitPanel, tmPanelContainer, southHeight);
		}
		else{
			southHeight = splitPanel.getWidgetContainerElement(tmPanelContainer).getOffsetHeight();
			SplitLayoutPanelHelper.setSplitPosition(splitPanel, tmPanelContainer, 0);
		}
		splitPanel.animate(500);
	}
	
	@Override
	public HasClickHandlers getShowTMViewButton() {
		return showTmViewLink;
	}
	
	@Override
	public void setShowTMViewButtonVisible(boolean visible) {
		showTmViewLink.setVisible(visible);
	}

}
