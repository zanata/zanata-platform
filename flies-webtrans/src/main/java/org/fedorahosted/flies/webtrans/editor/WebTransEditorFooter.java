package org.fedorahosted.flies.webtrans.editor;


import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.inject.Inject;

public class WebTransEditorFooter extends HorizontalPanel{

	private final Pager pager;
	private final StatusBar statusBar;
	private final Label messages;
	
	public WebTransEditorFooter(Pager pager, StatusBar statusBar) {
		setStylePrimaryName("WebTransEditor");
		addStyleDependentName("footer");
		this.pager = pager;
		
		setHeight("20px");
		setWidth("100%");
		setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		
		messages = new Label("[Messages goes here]");
		add(messages);
		setCellHorizontalAlignment(messages, HorizontalPanel.ALIGN_LEFT);

		add(pager);
		setCellHorizontalAlignment(pager, HorizontalPanel.ALIGN_CENTER);
		
		//status = new Label("[Status goes here]");
		this.statusBar = statusBar;
		this.statusBar.setWidth("200px");
		add(this.statusBar);
		setCellHorizontalAlignment(statusBar, HorizontalPanel.ALIGN_RIGHT);
	}
	
}
