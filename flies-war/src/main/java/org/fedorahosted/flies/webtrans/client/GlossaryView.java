package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.Concept;
import org.fedorahosted.flies.webtrans.client.ui.HighlightingLabel;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class GlossaryView extends FlowPanel implements GlossaryPresenter.Display {
	private static final int CELL_PADDING = 5;
	private static final int HEADER_ROW = 0;
	private static final int TERM_COL = 0;
	private static final int TRANSLATION_COL = 1;
	private static final int DESCRIPT_COL = 2;

	private final TextBox glTextBox = new TextBox();
	private final Button searchButton = new Button("Search");
	private final FlexTable resultTable = new FlexTable();
	private final GlossaryUpload fileUpload = new GlossaryUpload();
	
	public GlossaryView() {
		glTextBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					searchButton.click();
				}
			}
		});

		add(fileUpload);
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
		addColumn("Term", TERM_COL);
		addColumn("Translation", TRANSLATION_COL);
		addColumn("Description", DESCRIPT_COL);

		int row = HEADER_ROW;
		for(final Concept concept: concepts) {
			++row;
			final String source = concept.getTerm();
			final String target = concept.getEntry().getTerm();
			final String desc = concept.getDesc();
			resultTable.setWidget(row, TERM_COL, new HighlightingLabel(source));
			resultTable.setWidget(row, TRANSLATION_COL, new HighlightingLabel(target));
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
