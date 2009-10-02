package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Label;

public class FilterPanel extends ContentPanel{
	
	public FilterPanel() {
		setHeading("Search and Filter");
		setHeight(250);
		setFrame(false);
		setCollapsible(true);
		add(new Label("Search options"), new RowData(1, -1, new Margins(4)));
	}

}
