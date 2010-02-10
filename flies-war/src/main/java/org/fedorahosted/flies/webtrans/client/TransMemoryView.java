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
		addColumn("Source", 0);
		addColumn("Target", 1);
		addColumn("Document", 2);
		addColumn("Score", 3);
		addColumn("Action", 4);

		int row = 1;
		for(final TransMemory memory: memories) {
			final String sourceResult = memory.getSource();
			final String targetResult = memory.getMemory();
			resultTable.setWidget(row, 0, new HighlightingLabel(sourceResult, ParserSyntax.MIXED));
			resultTable.setWidget(row, 1, new HighlightingLabel(targetResult, ParserSyntax.MIXED));
			
			Anchor copyLink = new Anchor("Copy To Target");
			copyLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					fireEvent(new TransMemoryCopyEvent(sourceResult, targetResult));
					Log.info("TransMemoryCopyEvent event is sent. (" + targetResult + ")");
				}
			});
			resultTable.setWidget(row, 4, copyLink);
			
			row++;
		}
		resultTable.setCellPadding(5);
	}
	
	private void addColumn(String columnHeading, int pos) {
	    Label widget = new Label(columnHeading);
	    widget.setWidth("100%");
	    widget.addStyleName("TransMemoryTableColumnHeader");
	    resultTable.setWidget(0, pos, widget);
	  }
	
	@Override
	public void clearResults() {
		resultTable.removeAllRows();
	}
}

