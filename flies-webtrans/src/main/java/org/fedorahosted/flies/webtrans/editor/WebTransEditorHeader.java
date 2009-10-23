package org.fedorahosted.flies.webtrans.editor;


import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class WebTransEditorHeader extends HorizontalPanel{

	public WebTransEditorHeader() {
		setStylePrimaryName("WebTransEditor");
		addStyleDependentName("header");
		setHeight("20px");
		setWidth("100%");
		add(new Label("[]"));
		add(new Label("[Filter]"));
		add(new Label("[Additional Toolbar items]"));
		
	}
}
