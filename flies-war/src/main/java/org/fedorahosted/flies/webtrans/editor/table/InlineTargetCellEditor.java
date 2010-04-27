package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.webtrans.client.Resources;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.client.InlineCellEditor.InlineCellEditorImages;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class InlineTargetCellEditor implements CellEditor<TransUnit>{

	/**
	 * An {@link ImageBundle} that provides images for {@link InlineTargetCellEditor}.
	 */
	public static interface TargetCellEditorImages extends
			InlineCellEditorImages {

	}

	/**
	 * Default style name.
	 */
	public static final String DEFAULT_STYLENAME = "gwt-TargetCellEditor";

	/**
	 * The click listener used to accept.
	 */
	private ClickHandler cancelHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			cancelEdit();
		}
	};

	/**
	 * The click listener used to accept.
	 */
	private ClickHandler cloneHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			// Set textArea back focused after the cloning.
			textArea.setText(cellValue.getSource());
			textArea.setFocus(true);
			isFocused = true;
			Log.info("InlineTargetCellEditor.java: Cloned content of source to target.");
		}
	};
	
	private final CheckBox toggleFuzzy;
	
	/**
	 * The click listener used to accept.
	 */
	private ClickHandler acceptHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			acceptEdit();
			gotoNextRow(curRow);
		}
	};
	
	/**
	 * The current {@link CellEditor.Callback}.
	 */
	private Callback<TransUnit> curCallback = null;
	
	private CancelCallback<TransUnit> cancelCallback = null;
	
	private EditRowCallback editRowCallback = null;

	/**
	 * The current {@link CellEditor.CellEditInfo}.
	 */
	private CellEditInfo curCellEditInfo = null;

	/**
	 * The main grid used for layout.
	 */
	private FlowPanel layoutTable;

	private Widget cellViewWidget;

	private TransUnit cellValue;
	
	private final TextArea textArea;
	
	private boolean isFocused = false;
	
	private Image stateImage;
	
	private int curRow;
	private int curCol;
	private HTMLTable table;
	
	private Resources resources = GWT.create(Resources.class);
	
	/*
	 * The minimum height of the target editor
	 */
	private static final int MIN_HEIGHT = 48;
	
	private final NavigationMessages messages;
	
	/**
	 * Construct a new {@link InlineTargetCellEditor}.
	 * 
	 * @param content
	 *            the {@link Widget} used to edit
	 */
	public InlineTargetCellEditor(final NavigationMessages messages, CancelCallback<TransUnit> callback, EditRowCallback tranValueCallback) {
		this(messages, GWT.<TargetCellEditorImages> create(TargetCellEditorImages.class), callback, tranValueCallback);
	}

	/**
	 * Construct a new {@link InlineTargetCellEditor} with the specified images.
	 * 
	 * @param content
	 *            the {@link Widget} used to edit
	 * @param images
	 *            the images to use for the accept/cancel buttons
	 */
	public InlineTargetCellEditor(final NavigationMessages messages, TargetCellEditorImages images, CancelCallback<TransUnit> callback,EditRowCallback rowCallback ) {
		this.messages = messages;
		// Wrap contents in a table
		layoutTable = new FlowPanel();

		cancelCallback = callback;
		editRowCallback = rowCallback;
		textArea = new TextArea();
		textArea.setStyleName("TableEditorContent-Edit");
		textArea.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				isFocused = false;				
			}
			
		});
		textArea.addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				isFocused = true;				
			}
			
		});
		textArea.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				// NB: if you change these, please change NavigationConsts too!
				if(event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					acceptEdit();
					gotoNextRow(curRow);
				} else if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					cancelEdit();
				} else if(event.isControlKeyDown() && event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN) { // was alt-e
					handleNextState(ContentState.NeedReview);
				} else if(event.isControlKeyDown() && event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP) { // was alt-m
					handlePrevState(ContentState.NeedReview);
//				} else if(event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN) { // bad in Firefox
				} else if(event.isAltKeyDown() && event.isDownArrow()) {
					handleNext();
//				} else if(event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP) { // bad in Firefox
				} else if(event.isAltKeyDown() && event.isUpArrow()) {
					handlePrev();
				} else if(event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN) { //alt-down
					handleNextState(ContentState.New);
				} else if(event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP) { // alt-up
					handlePrevState(ContentState.New);
				}
			}

		});
		layoutTable.add(textArea);
		
		HorizontalPanel operationsPanel = new HorizontalPanel();
		operationsPanel.addStyleName("float-right-div");
		operationsPanel.setSpacing(4);
		layoutTable.add(operationsPanel);
		
		// icon as the current state of the unit
		stateImage = new Image( resources.newUnit() );
		operationsPanel.add(stateImage);

		// Add content widget
		toggleFuzzy = new CheckBox("Fuzzy");
		operationsPanel.add(toggleFuzzy);
		
		PushButton cancelButton = new PushButton(images.cellEditorCancel().createImage(),cancelHandler);
		cancelButton.setText( messages.editCancel() );
		cancelButton.setTitle( messages.editCancelShortcut() );
		operationsPanel.add(cancelButton);
		
		PushButton cloneButton = new PushButton(images.cellEditorCancel().createImage(),cloneHandler);
		cloneButton.setText( messages.editClone() );
		cloneButton.setTitle( messages.editCloneShortcut() );
		operationsPanel.add(cloneButton);

		PushButton acceptButton = new PushButton(images.cellEditorAccept().createImage(),acceptHandler);
		acceptButton.setText( messages.editSave() );
		acceptButton.setTitle( messages.editSaveShortcut() );
		operationsPanel.add(acceptButton);
	}

	private void gotoNextRow(int row) {
		editRowCallback.gotoNextRow(row);
	}
	
	private void gotoPrevRow(int row) {
		editRowCallback.gotoPrevRow(row);
	}
	
	private void gotoNextFuzzy(int row, ContentState state) {
		editRowCallback.gotoNextFuzzy(row, state);
	}
	
	private void gotoPrevFuzzy(int row, ContentState state) {
		editRowCallback.gotoPrevFuzzy(row, state);
	}

	private void restoreView() {
		if(curCellEditInfo != null && cellViewWidget != null) {
			curCellEditInfo.getTable().setWidget(curRow, curCol, cellViewWidget);
			cellViewWidget.getParent().setHeight(cellViewWidget.getOffsetHeight()+"px");
		}
	}
	
	private boolean isDirty(){
		if(cellValue == null) return false;
		return !textArea.getText().equals(cellValue.getTarget()) ;
	}
	
	public boolean isEditing() {
		return cellValue != null;
	}
	
	public boolean isFocused() {
		return isFocused;
	}
	
	public void setText(String text) {
		if (isEditing()) {
			textArea.setText(text);
		}
	}
	
	public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue,
			Callback<TransUnit> callback) {
        
		// don't allow edits of two cells at once
		if( isDirty() ) {
	    	callback.onCancel(cellEditInfo);
	    	return;
	    }
		
		if( isEditing() ){
			if(cellEditInfo.getCellIndex() == curCol && cellEditInfo.getRowIndex() == curRow){
				return;
			}
			restoreView();
		}
		
		Log.debug("starting edit of cell");
		
		// Save the current values
		curCallback = callback;
		curCellEditInfo = cellEditInfo;

		// Get the info about the cell
		table = curCellEditInfo.getTable();

		curRow = curCellEditInfo.getRowIndex();
		curCol = curCellEditInfo.getCellIndex();

		cellViewWidget = table.getWidget(curRow, curCol);

		int height = table.getWidget(curRow, curCol-1).getOffsetHeight();
		
		int realHeight = height > MIN_HEIGHT ? height : MIN_HEIGHT;
		
		textArea.setHeight(realHeight+"px");
		
		int width = table.getWidget(curRow, curCol-1).getOffsetWidth() - 10;
		textArea.setWidth(width+"px");
		
		table.setWidget(curRow, curCol, layoutTable);
		textArea.setText(cellValue.getTarget());
		
		this.cellValue = cellValue;
		textArea.setFocus(true);
		DOM.scrollIntoView(textArea.getElement());
		toggleFuzzy.setValue(cellValue.getStatus() == ContentState.NeedReview);
		refreshStateImage();
	}

	private void refreshStateImage() {
		if (cellValue.getStatus() == ContentState.New)
			stateImage.setUrl( resources.newUnit().getURL() );
		else if (cellValue.getStatus() == ContentState.NeedReview)
			stateImage.setUrl( resources.fuzzyUnit().getURL() );
		else if (cellValue.getStatus() == ContentState.Approved)
			stateImage.setUrl( resources.approvedUnit().getURL() );
	}

	/**
	 * Accept the contents of the cell editor as the new cell value.
	 */
	protected void acceptEdit() {
		// Check if we are ready to accept
		if (!onAccept()) {
			return;
		}
		cellValue.setTarget(textArea.getText());
		if(cellValue.getTarget().isEmpty())
			cellValue.setStatus(ContentState.New);
		else
			cellValue.setStatus(toggleFuzzy.getValue()? ContentState.NeedReview : ContentState.Approved) ;
		restoreView();
		
		// Send the new cell value to the callback
		curCallback.onComplete(curCellEditInfo, cellValue);
		clearSelection();
	}
	
	private void clearSelection() {
		curCallback = null;
		curCellEditInfo = null;
		cellViewWidget = null;
		cellValue = null;
	}

	/**
	 * Cancel the cell edit.
	 */
	protected void cancelEdit() {
		// Fire the event
		if (!onCancel()) {
			return;
		}

		restoreView();
		
		// Call the callback
		if (curCallback != null) {
			//curCallback.onCancel(curCellEditInfo);
			cancelCallback.onCancel(cellValue);
		}

		clearSelection();
	}

	/**
	 * Called before an accept takes place.
	 * 
	 * @return true to allow the accept, false to prevent it
	 */
	protected boolean onAccept() {
		return true;
	}

	/**
	 * Called before a cancel takes place.
	 * 
	 * @return true to allow the cancel, false to prevent it
	 */
	protected boolean onCancel() {
		return true;
	}

	public void handleNext() {
		gotoNextRow(curRow);
	}

	public void handlePrev() {
		gotoPrevRow(curRow);
	}

	public void handleNextState(ContentState state) {
		gotoNextFuzzy(curRow, state);
	}

	public void handlePrevState(ContentState state) {
		gotoPrevFuzzy(curRow, state);
	}

	//	public void handleNextNew() {
//		cancelEdit();
//		incRow();
//		gotoNextFuzzy(row, ContentState.New);
//	}
//
//	public void handlePrevNew() {
//		cancelEdit();
//		decRow();
//		gotoPrevFuzzy(row, ContentState.New);
//	}

}
