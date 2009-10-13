package org.fedorahosted.flies.webtrans.editor;


import org.fedorahosted.flies.webtrans.client.ui.Pager;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class WebTransEditorFooter extends HorizontalPanel{

	public WebTransEditorFooter() {
		setHeight("20px");
		add(new Label("[Hello World]"));
		add(new Pager());
		add(new Label("[Filter]"));
		add(new Label("[View]"));
		
	}
}
