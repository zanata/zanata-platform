package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.webtrans.client.TranslationEditorView.TranslationEditorViewUiBinder;
import org.fedorahosted.flies.webtrans.client.ui.HighlightingLabel;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
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
	private static final int ACTION_COL = 2;

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
		addColumn("Action", ACTION_COL);
		
		int row = HEADER_ROW;
		for(final TransMemory memory: memories) {
			++row;
			final String sourceMessage = memory.getSource();
			final String targetMessage = memory.getMemory();
			final String sourceComment = memory.getSourceComment();
			final String targetComment = memory.getTargetComment();
			final String docID = memory.getDocID();

			resultTable.setWidget(row, SOURCE_COL, new HighlightingLabel(sourceMessage));
			resultTable.setWidget(row, TARGET_COL, new HighlightingLabel(targetMessage));

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
	
	public void clearResults() {
		resultTable.removeAllRows();
	}
}

