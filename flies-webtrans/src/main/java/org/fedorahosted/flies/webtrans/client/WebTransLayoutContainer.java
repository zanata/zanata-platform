package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.mvp.HighlightingCellEditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.weborient.codemirror.client.CodeMirrorEditorWidget;
import com.weborient.codemirror.client.ParserSyntax;

public class WebTransLayoutContainer extends Composite {

	public WebTransLayoutContainer() {

		VerticalPanel vPanel = new VerticalPanel();
		initWidget(vPanel);
		
		Panel syntaxBar = new HorizontalPanel();
		syntaxBar.add(new Label("Highlighting: "));
		RadioButton noneButton = new RadioButton("syntaxBar", "None");
		noneButton.setValue(true, false);
		syntaxBar.add(noneButton);
		RadioButton jsButton = new RadioButton("syntaxBar", "JavaScript");
		syntaxBar.add(jsButton);
		RadioButton xmlButton = new RadioButton("syntaxBar", "XML");
		syntaxBar.add(xmlButton);
		RadioButton mixedButton = new RadioButton("syntaxBar", "HTML");
		syntaxBar.add(mixedButton);
		
		vPanel.add(syntaxBar);

		final CodeMirrorEditorWidget targetWidget;
		targetWidget = HighlightingCellEditor.createWidget();
		targetWidget.setText("Translation text (\"text flow target\"/<text-flow-target>)");
		noneButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setSyntax(ParserSyntax.NONE);
			}});
		jsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setSyntax(ParserSyntax.JAVASCRIPT);
			}});
		xmlButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setSyntax(ParserSyntax.XML);
			}});
		mixedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				targetWidget.setSyntax(ParserSyntax.MIXED);
			}});

		vPanel.add(targetWidget);
	}

}