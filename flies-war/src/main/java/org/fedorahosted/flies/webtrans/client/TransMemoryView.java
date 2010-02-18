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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class TransMemoryView extends FlowPanel implements TransMemoryPresenter.Display {

	private static final int CELL_PADDING = 5;
	private static final int HEADER_ROW = 0;
	private static final int SOURCE_COL = 0;
	private static final int TARGET_COL = 1;
	private static final int DOCUMENT_COL = 2;
	private static final int ACTION_COL = 3;
	
	private final TextBox tmTextBox = new TextBox();
	private final CheckBox fuzzyButton = new CheckBox("Fuzzy");
	private final Button searchButton = new Button("Search");
	private final Button clearButton = new Button("Clear");
	
	private final FlexTable resultTable = new FlexTable();
	public TransMemoryView() {
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
		add(fuzzyButton);
		add(searchButton);
		add(clearButton);
		add(resultTable);
	}
	
	@Override
	public HasValue<Boolean> getFuzzyButton() {
		return fuzzyButton;
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
		addColumn("Source", SOURCE_COL);
		addColumn("Target", TARGET_COL);
		addColumn("Document", DOCUMENT_COL);
		addColumn("Action", ACTION_COL);

		int row = HEADER_ROW;
		for(final TransMemory memory: memories) {
			++row;
			final String sourceResult = memory.getSource();
			final String targetResult = memory.getMemory();
			resultTable.setWidget(row, SOURCE_COL, new HighlightingLabel(sourceResult, ParserSyntax.MIXED));
			resultTable.setWidget(row, TARGET_COL, new HighlightingLabel(targetResult, ParserSyntax.MIXED));
			resultTable.setText(row, DOCUMENT_COL, memory.getDocID());
			
			Anchor copyLink = new Anchor("Copy To Target");
			copyLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					fireEvent(new TransMemoryCopyEvent(sourceResult, targetResult));
					Log.info("TransMemoryCopyEvent event is sent. (" + targetResult + ")");
				}
			});
			resultTable.setWidget(row, ACTION_COL, copyLink);
		}
		resultTable.setCellPadding(CELL_PADDING);
	}
	
	private void addColumn(String columnHeading, int pos) {
	    Label widget = new Label(columnHeading);
	    widget.setWidth("100%");
	    widget.addStyleName("TransMemoryTableColumnHeader");
	    resultTable.setWidget(HEADER_ROW, pos, widget);
	  }
	
	@Override
	public void clearResults() {
		resultTable.removeAllRows();
	}
}

