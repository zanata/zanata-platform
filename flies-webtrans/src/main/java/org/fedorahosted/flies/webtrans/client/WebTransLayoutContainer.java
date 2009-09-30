package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class WebTransLayoutContainer extends LayoutContainer {

	ContentPanel west;
	ContentPanel east;
	ContentPanel center;

	public WebTransLayoutContainer() {
		final BorderLayout layout = new BorderLayout();

		setLayout(layout);
		setBorders(false);

		west = new ContentPanel();
		west.setHeaderVisible(false);
		west.setLayout(new RowLayout(Orientation.VERTICAL));
		west.add(new DocumentListPanel(), new RowData(1, -1, new Margins(4)));
		west.add(new TranslatorsPanel(), new RowData(1, -1, new Margins(4)));

		center = new ContentPanel();
		center.setHeaderVisible(false);
		center.setScrollMode(Scroll.AUTOX);

		east = new ContentPanel();
		east.setHeaderVisible(false);

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
		westData.setSplit(true);
		westData.setMinSize(1);
		westData.setCollapsible(false);
		westData.setMargins(new Margins(5));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(5, 0, 5, 0));

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 200);
		eastData.setSplit(true);
		eastData.setMinSize(1);
		eastData.setCollapsible(false);
		eastData.setMargins(new Margins(5));

		add(west, westData);
		add(center, centerData);
		add(east, eastData);
	}

}