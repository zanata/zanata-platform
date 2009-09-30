package org.fedorahosted.flies.webtrans.client;


import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.widgetideas.table.client.CachedTableController;
import com.google.gwt.widgetideas.table.client.EditablePagingGrid;
import com.google.gwt.widgetideas.table.client.FixedWidthFlexTable;
import com.google.gwt.widgetideas.table.client.FixedWidthGrid;
import com.google.gwt.widgetideas.table.client.PagingScrollTable;
import com.google.gwt.widgetideas.table.client.ScrollTable;
import com.google.gwt.widgetideas.table.client.SelectionGrid;
import com.google.gwt.widgetideas.table.client.SortableFixedWidthGrid;
import com.google.gwt.widgetideas.table.client.overrides.FlexTable.FlexCellFormatter;

public class TranslationPanel extends Composite {

	public TranslationPanel() {
		initWidget(new Label("hello world"));
	}

}
