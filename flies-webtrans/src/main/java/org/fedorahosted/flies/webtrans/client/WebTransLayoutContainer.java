package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.weborient.codemirror.client.CodeMirrorConfiguration;
import com.weborient.codemirror.client.CodeMirrorEditorWidget;

public class WebTransLayoutContainer extends LayoutContainer {

	ContentPanel west;
	ContentPanel east;
	ContentPanel center;
	private CodeMirrorEditorWidget sourceWidget;
	private CodeMirrorEditorWidget targetWidget;

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
		sourceWidget = getEditorWidget(true);
		center.add(sourceWidget);

		east = new ContentPanel();
		east.setHeaderVisible(false);
		targetWidget = getEditorWidget(false);
		east.add(targetWidget);

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

	
	CodeMirrorEditorWidget getEditorWidget(boolean readOnly) {
        
        // use the configuration class in order 
        // to override the default widget configuraions

        CodeMirrorConfiguration configuration = new CodeMirrorConfiguration();
        configuration.setTagSelectorLabel("Templates: ");
        configuration.setListBoxPreSets("<html></html>", "<div></div>");
        configuration.setLineNumbers(false);
        configuration.setTextWrapping(true);
        configuration.setReadOnly(readOnly);
   
        // pass the configuration object to the widget

        return new CodeMirrorEditorWidget(configuration);
   }

	// this should be called after container is added to RootPanel
	public void init() {
		// NB: These editor widget methods currently can't be called until
		// the JSNI is initialized by onLoad() 
		targetWidget.setText("Target text");
		sourceWidget.setText("This is the source text (text flow)");
	}	
	
}