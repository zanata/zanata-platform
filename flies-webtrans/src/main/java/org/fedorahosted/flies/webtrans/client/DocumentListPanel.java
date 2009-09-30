package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Label;

public class DocumentListPanel extends ContentPanel {

	public DocumentListPanel() {
	    setHeading("Documents");  
	    setLayout(new RowLayout(Orientation.VERTICAL));  
	    add(new Label("hello world"));
	}
}
