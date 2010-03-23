package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.DocName;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

public class DocumentNode extends Node<DocName> {

	private static DocumentNodeUiBinder uiBinder = GWT
			.create(DocumentNodeUiBinder.class);

	interface DocumentNodeUiBinder extends UiBinder<Widget, DocumentNode> {
	}

	interface Styles extends CssResource {
		String mouseOver();
		String selected();
	}

	private static final HyperlinkImpl impl = new HyperlinkImpl();
	
	@UiField
	Label documentLabel;
	
	@UiField(provided=true)
	final Resources resources;
	
	@UiField(provided=true)
	final FlowPanel rootPanel;
	
	@UiField
	Styles style;
	
	final WebTransMessages messages;
	
	public DocumentNode(Resources resources, WebTransMessages messages) {
		this.resources = resources;
		this.messages = messages;
		
		
		rootPanel = new FlowPanel() {
			public void onBrowserEvent(Event event) {
				switch(event.getTypeInt()) {
				case Event.ONMOUSEOVER:
					addStyleName(style.mouseOver());
					break;
				case Event.ONMOUSEOUT:
					removeStyleName(style.mouseOver());
					break;
				case Event.ONCLICK:
					if(event.getButton() == NativeEvent.BUTTON_LEFT && impl.handleAsClick(event)) {
						ClickEvent.fireNativeEvent(event, this);
					}
				}

				super.onBrowserEvent(event);
			};
		};
		
		initWidget(uiBinder.createAndBindUi(this));
		
		rootPanel.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONCLICK);
		
	}
	
	public DocumentNode(Resources resources, WebTransMessages messages, DocName doc) {
		this(resources, messages);
		setDataItem(doc);
	}

	public DocumentNode(Resources resources, WebTransMessages messages, DocName doc, ClickHandler clickHandler) {
		this(resources, messages, doc);
		addHandler(clickHandler, ClickEvent.getType());
	}
	
	public void refresh() {
		rootPanel.getElement().setId("doc-#"+getDataItem().getId().toString());
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
