package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.weborient.codemirror.client.CodeMirrorConfiguration;
import com.weborient.codemirror.client.CodeMirrorEditorWidget;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.SyntaxLanguage;

public class WebTransLayoutContainer extends Composite {

	public WebTransLayoutContainer() {

		VerticalPanel vPanel = new VerticalPanel();
		initWidget(vPanel);
		
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
		
		vPanel.add(langBar);
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
			
			vPanel.add(sourceWidget);
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

		vPanel.add(targetWidget);
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