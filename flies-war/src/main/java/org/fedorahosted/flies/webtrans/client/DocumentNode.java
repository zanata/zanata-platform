package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount.LabelFormat;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DocumentNode extends Node<DocName> {

	private static DocumentNodeUiBinder uiBinder = GWT
			.create(DocumentNodeUiBinder.class);

	interface DocumentNodeUiBinder extends UiBinder<Widget, DocumentNode> {
	}

	interface Styles extends CssResource {
		String mouseOver();
		String selected();
	}
	
	@UiField
	Label documentLabel;
	
	@UiField(provided=true)
	final Resources resources;
	
	@UiField(provided=true)
	final FlowPanel rootPanel;
	
	@UiField
	Styles style;
	
	@UiField 
	Anchor translateLink;
	
	@UiField(provided=true)
	StatusBar statusBar;
	
	final WebTransMessages messages;
	
	public DocumentNode(Resources resources, WebTransMessages messages) {
		this.resources = resources;
		this.messages = messages;
		
		this.statusBar = new StatusBar(messages);
		statusBar.setToggleEnabled(false);
		statusBar.setLabelFormat(LabelFormat.Percentage);
		rootPanel = new FlowPanel() {
			public void onBrowserEvent(Event event) {
				switch(event.getTypeInt()) {
				case Event.ONMOUSEOVER:
					addStyleName(style.mouseOver());
					break;
				case Event.ONMOUSEOUT:
					removeStyleName(style.mouseOver());
					break;
				}

				super.onBrowserEvent(event);
			};
		};
		
		initWidget(uiBinder.createAndBindUi(this));
		
		rootPanel.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		
	}
	
	public DocumentNode(Resources resources, WebTransMessages messages, DocName doc) {
		this(resources, messages);
		translateLink.getElement().setId("link_translate-doc#"+ doc.getId().toString());
		setDataItem(doc);
	}

	public DocumentNode(Resources resources, WebTransMessages messages, DocName doc, ClickHandler clickHandler) {
		this(resources, messages, doc);
		translateLink.addClickHandler(clickHandler);
	}
	
	public void refresh() {
		this.getElement().setId("doc-#"+getDataItem().getId().toString());
		documentLabel.setText(getDataItem().getName());
	}

	@Override
	boolean isDocument() {
		return true;
	}

	public void setSelected(boolean selected) {
		if(selected) {
			rootPanel.addStyleName(style.selected());
		}
		else {
			rootPanel.removeStyleName(style.selected()); 
		}
		
	}
}
