package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.Concept;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.weborient.codemirror.client.HighlightingLabel;
import com.weborient.codemirror.client.ParserSyntax;

public class GlossaryView extends FlowPanel implements GlossaryPresenter.Display {
	private static final int CELL_PADDING = 5;
	private static final int HEADER_ROW = 0;
	private static final int SOURCE_COL = 0;
	private static final int TARGET_COL = 1;
	private static final int DESCRIPT_COL = 2;

	private final Label glLabel = new Label("Input the term");
	private final TextBox glTextBox = new TextBox();
	private final Button searchButton = new Button("Search");
	private final FlexTable resultTable = new FlexTable();
	
	public GlossaryView() {
		glTextBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					searchButton.click();
				}
			}
		});
	
		add(glLabel);
		add(glTextBox);
		add(searchButton);
		add(resultTable);
	}
	
	@Override
	public Button getSearchButton() {
		return searchButton;
	}
	
	public TextBox getGlossaryTextBox() {
		return glTextBox;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopProcessing() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void createTable(ArrayList<Concept> concepts) {
		clearResults();
		addColumn("Term", SOURCE_COL);
		addColumn("Explain", TARGET_COL);
		addColumn("Description", DESCRIPT_COL);

		int row = HEADER_ROW;
		for(final Concept concept: concepts) {
			++row;
			final String source = concept.getTerm();
			final String target = concept.getEntry().getTerm();
			final String desc = concept.getDesc();
			resultTable.setWidget(row, SOURCE_COL, new HighlightingLabel(source, ParserSyntax.MIXED));
			resultTable.setWidget(row, TARGET_COL, new HighlightingLabel(target, ParserSyntax.MIXED));
			resultTable.setText(row, DESCRIPT_COL, desc);
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
