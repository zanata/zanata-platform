package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.webtrans.client.events.TransMemoryCopyEvent;
import org.fedorahosted.flies.webtrans.client.ui.HighlightingLabel;
import org.fedorahosted.flies.webtrans.shared.model.TransMemory;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransMemoryView extends Composite implements TransMemoryPresenter.Display {

	private static final int CELL_PADDING = 5;
	private static final int HEADER_ROW = 0;
	private static final int SOURCE_COL = 0;
	private static final int TARGET_COL = 1;
	private static final int SCORE_COL = 2;
	private static final int SIMILARITY_COL = 3;
	private static final int ACTION_COL = 4;

	private static TransMemoryViewUiBinder uiBinder = GWT
		.create(TransMemoryViewUiBinder.class);

	interface TransMemoryViewUiBinder extends
		UiBinder<Widget, TransMemoryView> {
	}
	
	@UiField
	TextBox tmTextBox;
	
	@UiField
	CheckBox phraseButton;
	
	@UiField
	Button searchButton;
	
	@UiField
	Button clearButton;
	
	@UiField
	FlexTable resultTable;
	
	@Inject
	private EventBus eventBus;
	
	private final WebTransMessages messages;
	
	NumberFormat scoreFormat = NumberFormat.getFormat("#.##");
	
	@Inject
	public TransMemoryView(final WebTransMessages messages) {
		this.messages = messages;
		initWidget( uiBinder.createAndBindUi(this));
		phraseButton.setText( messages.tmPhraseButtonLabel() );
		clearButton.setText( messages.tmClearButtonLabel() );
		searchButton.setText( messages.tmSearchButtonLabel() );
	}

	@UiHandler("tmTextBox")
	void onTmTextBoxKeyUp(KeyUpEvent event) {
		if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
			searchButton.click();
		}
	}

	@UiHandler("clearButton")
	void onClearButtonClicked(ClickEvent event) {
		tmTextBox.setText("");
		clearResults();
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
		clearResults();
		resultTable.setWidget(0, 0, new Label("Loading..."));
	}

	@Override
	public void stopProcessing() {
	}
	
	@Override
	public void createTable(ArrayList<TransMemory> memories) {
		clearResults();
		addColumn("Source", SOURCE_COL);
		addColumn("Target", TARGET_COL);
		addColumn("Relevance", SCORE_COL);
		addColumn("Similarity", SIMILARITY_COL);
		addColumn("Action", ACTION_COL);
		
		int row = HEADER_ROW;
		for(final TransMemory memory: memories) {
			++row;
			final String sourceMessage = memory.getSource();
			final String targetMessage = memory.getMemory();
			final String sourceComment = memory.getSourceComment();
			final String targetComment = memory.getTargetComment();
			final String docID = memory.getDocID();
			final float score = memory.getRelevanceScore();
			final int similarity = memory.getSimilarityPercent();

			resultTable.setWidget(row, SOURCE_COL, new HighlightingLabel(sourceMessage));
			resultTable.setWidget(row, TARGET_COL, new HighlightingLabel(targetMessage));
			String scoreString = scoreFormat.format(score);
			resultTable.setText(row, SCORE_COL, scoreString);
			resultTable.setText(row, SIMILARITY_COL, String.valueOf(similarity));

			final Anchor copyLink = new Anchor("Copy");
			copyLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					eventBus.fireEvent(new TransMemoryCopyEvent(sourceMessage, targetMessage));
					Log.info("TransMemoryCopyEvent event is sent. (" + targetMessage + ")");
				}
			});
			resultTable.setWidget(row, ACTION_COL, copyLink);
			// comments are presently disabled on the server side
//			String suppInfo = "Source Comment: " + sourceComment + "     "
//            + "Target Comment: " + targetComment + "     "
//            + "Document Name: " + docID;
			String suppInfo = "Document Name: " + docID;

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
	
	public void clearResults() {
		resultTable.removeAllRows();
	}
}

