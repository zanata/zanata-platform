package org.fedorahosted.flies.webtrans.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.weborient.codemirror.client.CodeMirrorConfiguration;
import com.weborient.codemirror.client.CodeMirrorEditorWidget;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.SyntaxLanguage;

public class WebTransLayoutContainer extends LayoutContainer {

	ContentPanel west;
	ContentPanel east;
	ContentPanel center;

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
		Panel langBar = new HorizontalPanel();
		langBar.add(new Label("Highlighting: "));
		RadioButton noneButton = new RadioButton("langBar", "None");
		noneButton.setValue(true, false);
		langBar.add(noneButton);
		RadioButton jsButton = new RadioButton("langBar", "JavaScript");
		langBar.add(jsButton);
		RadioButton xmlButton = new RadioButton("langBar", "XML");
		langBar.add(xmlButton);
		RadioButton mixedButton = new RadioButton("langBar", "HTML");
		langBar.add(mixedButton);
		center.add(langBar);
		
		for (int i=0; i<5; i++) {
			
			String sourceText = "Original/source text (\"text flow\"/<text-flow>) #"+i;
			final HighlightingLabel sourceWidget = new HighlightingLabel(sourceText);
					
			noneButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent ce) {
					sourceWidget.setLanguage(SyntaxLanguage.NONE);
				}});
			jsButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent ce) {
					sourceWidget.setLanguage(SyntaxLanguage.JAVASCRIPT);
				}});
			xmlButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent ce) {
					sourceWidget.setLanguage(SyntaxLanguage.XML);
				}});
			mixedButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent ce) {
					sourceWidget.setLanguage(SyntaxLanguage.MIXED);
				}});
			
			center.add(sourceWidget);
		}

		final CodeMirrorEditorWidget targetWidget;
		targetWidget = getEditorWidget(false);
		targetWidget.setText("Translation text (\"text flow target\"/<text-flow-target>)");
		noneButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setLanguage(SyntaxLanguage.NONE);
			}});
		jsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setLanguage(SyntaxLanguage.JAVASCRIPT);
			}});
		xmlButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setLanguage(SyntaxLanguage.XML);
			}});
		mixedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setLanguage(SyntaxLanguage.MIXED);
			}});

		center.add(targetWidget);
		
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
        configuration.setLineNumbers(false);
        configuration.setTextWrapping(true);
        configuration.setReadOnly(readOnly);
        configuration.setToolbar(false);
   
        // pass the configuration object to the widget

        return new CodeMirrorEditorWidget(configuration);
   }

}