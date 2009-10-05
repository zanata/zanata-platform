package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.Command;
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

		west = new ContentPanel();
		west.setHeaderVisible(false);
		VBoxLayout westLayout = new VBoxLayout();  
		westLayout.setPadding(new Padding(5));  
		westLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);  
		west.setLayout(westLayout);
		
		west.add(new DocumentListPanel(), new VBoxLayoutData(new Margins(0, 0, 5, 0)));
		west.add(new FilterPanel(), new VBoxLayoutData(new Margins(0, 0, 5, 0)));
		
		VBoxLayoutData flex = new VBoxLayoutData(new Margins(0, 0, 5, 0));
		flex.setFlex(1);
		west.add(new Text(), flex);
		west.add(new TranslatorsPanel(), new VBoxLayoutData(new Margins(0)));

		center = new ContentPanel();//WebTransPanel();
		center.setLayout(new FillLayout());
		center.setHeaderVisible(false);
		center.setScrollMode(Scroll.AUTOY);
		east = new ContentPanel();
		east.setHeaderVisible(false);
		
		sourceWidget = getEditorWidget(true);
		center.add(sourceWidget);

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