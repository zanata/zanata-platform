package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.TransMemory;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class TransMemoryView extends FlowPanel implements TransMemoryPresenter.Display {

	private Button searchButton;
	private Button clearButton = new Button("Clear");
	private TextBox tmTextBox;
	
	private final FlexTable resultTable = new FlexTable();
	public TransMemoryView() {
		tmTextBox = new TextBox();
		searchButton = new Button("Search");
		
		tmTextBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					searchButton.click();
				}
			}
		});
		
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				tmTextBox.setText("");
				clearResults();
			}
		});
		
		add(tmTextBox);
		add(searchButton);
		add(clearButton);
		add(resultTable);
	}
	
	@Override
	public Button getSearchButton() {
		return searchButton;
	}
	
	public TextBox getTmTextBox() {
		return tmTextBox;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
	}

	@Override
	public void stopProcessing() {
	}
	
	@Override
	public void createTable(ArrayList<TransMemory> memories) {
		clearResults();
		Label sourceTitle = new Label("Source");
		Label targetTitle = new Label("Target");
		Label actionTitle = new Label("Action");
		sourceTitle.setStyleName("TransMemoryTableColumnHeader");
		targetTitle.setStyleName("TransMemoryTableColumnHeader");
		actionTitle.setStyleName("TransMemoryTableColumnHeader");
		resultTable.setWidget(0, 0, sourceTitle);
		resultTable.setWidget(0, 1, targetTitle);
		resultTable.setWidget(0, 2, actionTitle);
		int row = 1;
		for(final TransMemory memory: memories) {
			final String sourceResult = new String(memory.getSource());
			final String targetResult = new String(memory.getMemory());
			resultTable.setWidget(row, 0, new HighlightingLabel(sourceResult, ParserSyntax.MIXED));
			resultTable.setWidget(row, 1, new HighlightingLabel(targetResult, ParserSyntax.MIXED));
			
			Anchor copyLink = new Anchor("Copy To Target");
			copyLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					fireEvent(new TransMemoryCopyEvent(memory.getSource(), memory.getMemory()));
					Log.info("TransMemoryCopyEvent event is sent. (" + targetResult + ")");
				}
			});
			resultTable.setWidget(row, 2, copyLink);
			
			row++;
		}
		resultTable.setCellPadding(5);
	}
	
	@Override
	public void clearResults() {
		resultTable.removeAllRows();
	}
}

