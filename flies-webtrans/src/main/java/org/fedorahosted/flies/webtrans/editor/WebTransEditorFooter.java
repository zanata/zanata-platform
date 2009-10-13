package org.fedorahosted.flies.webtrans.editor;


import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.inject.Inject;

public class WebTransEditorFooter extends HorizontalPanel{

	private final Pager pager;
	private final Label status;
	private final Label messages;
	
	@Inject
	public WebTransEditorFooter(Pager pager) {
		this.pager = pager;
		
		setHeight("20px");
		setWidth("100%");
		setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		
		messages = new Label("[Messages goes here]");
		add(messages);
		setCellHorizontalAlignment(messages, HorizontalPanel.ALIGN_LEFT);

		add(pager);
		setCellHorizontalAlignment(pager, HorizontalPanel.ALIGN_CENTER);
		
		status = new Label("[Status goes here]");
		add(status);
		setCellHorizontalAlignment(status, HorizontalPanel.ALIGN_RIGHT);
	}
	
	public Pager getPager() {
		return pager;
	}
	
}
