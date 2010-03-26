package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A trick to allow resizing SplitLayoutPanel automagically
 * 
 * http://code.google.com/p/google-web-toolkit/issues/detail?id=4489
 * 
 */
public abstract class SplitLayoutPanelHelper extends SplitLayoutPanel {
	
	public static void setSplitPosition(SplitLayoutPanel splitPanel, Widget widgetBeforeTheSplitter, double size){
	     LayoutData layout =  (LayoutData) widgetBeforeTheSplitter.getLayoutData();
	     layout.oldSize = layout.size;
	     layout.size=size;
	  }
}
