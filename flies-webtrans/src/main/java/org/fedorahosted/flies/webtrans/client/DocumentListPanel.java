package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Label;

public class DocumentListPanel extends ContentPanel {

	public DocumentListPanel() {
		setHeading("Documents");
		setLayout(new RowLayout(Orientation.VERTICAL));

		TabPanel innerTab = new TabPanel();
		innerTab.setPlain(false);
		innerTab.setTabPosition(TabPosition.BOTTOM);

		TabItem treeTab = new TabItem("Tree");
		treeTab.add( new Label("replace this with a TreePanel"));
		innerTab.add(treeTab);
		TabItem listTab = new TabItem("List");
		listTab.addText("Just a plain old List");
		innerTab.add(listTab);

		add(innerTab);
	}

}
