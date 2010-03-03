package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

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
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class TransMemoryView extends FlowPanel implements TransMemoryPresenter.Display {

	private static final int CELL_PADDING = 5;
	private static final int HEADER_ROW = 0;
	private static final int SOURCE_COL = 0;
	private static final int TARGET_COL = 1;
	private static final int ACTION_COL = 2;
	
	private final TextBox tmTextBox = new TextBox();
	private final CheckBox phraseButton = new CheckBox("Exact");
	private final Button searchButton = new Button("Search");
	private final Button clearButton = new Button("Clear");

	final DecoratedPopupPanel resultSuppPanel = new DecoratedPopupPanel(true);
	
	private final FlexTable resultTable = new FlexTable();
	@Inject
	private EventBus eventBus;
	
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
		add(phraseButton);
		add(searchButton);
		add(clearButton);
		add(resultTable);
	}
	
	@Override
	public HasValue<Boolean> getExactButton() {
		return phraseButton;
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
		resultTable.setWidget(0, 0, new Label("Loading..."));
		Log.info("TMView: shows loading message.");
	}

	@Override
	public void stopProcessing() {
	}
	
	@Override
	public void createTable(ArrayList<TransMemory> memories) {
		clearResults();
		addColumn("Source", SOURCE_COL);
		addColumn("Target", TARGET_COL);
		addColumn("Action", ACTION_COL);
		
		int row = HEADER_ROW;
		for(final TransMemory memory: memories) {
			++row;
			final String sourceMessage = memory.getSource();
			final String targetMessage = memory.getMemory();
			final String sourceComment = memory.getSourceComment();
			final String targetComment = memory.getTargetComment();
			final String docID = memory.getDocID();

			resultTable.setWidget(row, SOURCE_COL, new HighlightingLabel(sourceMessage, ParserSyntax.MIXED));
			resultTable.setWidget(row, TARGET_COL, new HighlightingLabel(targetMessage, ParserSyntax.MIXED));

			final Anchor copyLink = new Anchor("Copy");
			copyLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					eventBus.fireEvent(new TransMemoryCopyEvent(sourceMessage, targetMessage));
					Log.info("TransMemoryCopyEvent event is sent. (" + targetMessage + ")");
				}
			});
			resultTable.setWidget(row, ACTION_COL, copyLink);
			String suppInfo = "Source Comment: " + sourceComment + "     "
            + "Target Comment: " + targetComment + "     "
            + "Document Name: " + docID;

			// Use ToolTips for supplementary info.
			resultTable.getWidget(row, SOURCE_COL).setTitle(suppInfo);				
			resultTable.getWidget(row, TARGET_COL).setTitle(suppInfo);
			resultTable.getWidget(row, ACTION_COL).setTitle("Copy \"" + targetMessage + "\" to the editor.");	
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

