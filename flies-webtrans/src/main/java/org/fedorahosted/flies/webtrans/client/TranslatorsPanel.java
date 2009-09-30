package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Label;

public class TranslatorsPanel extends ContentPanel {
	
	public TranslatorsPanel() {
	    setHeading("Translators");  
	    setLayout(new RowLayout(Orientation.VERTICAL));  
	    setFrame(false);  
	    setCollapsible(true);
	    add(new Label("hello world"));
	}

}
