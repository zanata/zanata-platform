package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.model.TransUnit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.client.InlineCellEditor.InlineCellEditorImages;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class InlineTransUnitCellEditor implements CellEditor<TransUnit> {

	/**
	 * An {@link ImageBundle} that provides images for {@link InlineTransUnitCellEditor}.
	 */
	public static interface TargetCellEditorImages extends
			InlineCellEditorImages {

	}

	/**
	 * <code>ClickDecoratorPanel</code> decorates any widget with the minimal
	 * amount of machinery to receive clicks for delegation to the parent.
	 */
	private static final class ClickDecoratorPanel extends SimplePanel {
		public ClickDecoratorPanel(Widget child, ClickHandler delegate) {
			setWidget(child);
			addDomHandler(delegate, ClickEvent.getType());
		}
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
			cancel();
		}
	};

	/**
	 * The click listener used to accept.
	 */
	private ClickHandler acceptHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			accept();
		}
	};

	/**
	 * The current {@link CellEditor.Callback}.
	 */
	private Callback<TransUnit> curCallback = null;

	/**
	 * The current {@link CellEditor.CellEditInfo}.
	 */
	private CellEditInfo curCellEditInfo = null;

	/**
	 * The main grid used for layout.
	 */
	private FlexTable layoutTable;

	private Widget cellWidget;

	private TransUnit cellValue;
	
	/**
	 * Construct a new {@link InlineTransUnitCellEditor}.
	 * 
	 * @param content
	 *            the {@link Widget} used to edit
	 */
	public InlineTransUnitCellEditor() {
		this(GWT.<TargetCellEditorImages> create(TargetCellEditorImages.class));
	}

	/**
	 * Construct a new {@link InlineTransUnitCellEditor} with the specified images.
	 * 
	 * @param content
	 *            the {@link Widget} used to edit
	 * @param images
	 *            the images to use for the accept/cancel buttons
	 */
	public InlineTransUnitCellEditor(TargetCellEditorImages images) {

		// Wrap contents in a table
		layoutTable = new FlexTable();
		FlexCellFormatter formatter = layoutTable.getFlexCellFormatter();
		layoutTable.setCellSpacing(0);

		// Add a label
		setLabel("");
		formatter.setColSpan(0, 0, 3);

		// Add content widget
		layoutTable.setWidget(1, 0, new TextArea());

		// Add accept and cancel buttons
		setAcceptWidget(images.cellEditorAccept().createImage());
		setCancelWidget(images.cellEditorCancel().createImage());
	}

	int row;
	int col;

	public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue,
			Callback<TransUnit> callback) {

		// don't allow edits of two cells at once
	    if(cellWidget != null) {
	    	callback.onCancel(cellEditInfo);
	    	return;
	    }
		
		// Save the current values
		curCallback = callback;
		curCellEditInfo = cellEditInfo;

		// Get the info about the cell
		HTMLTable table = curCellEditInfo.getTable();

		if (cellWidget != null && layoutTable.getParent() != null) {
			table.setWidget(row, col, cellWidget);
		}

		row = curCellEditInfo.getRowIndex();
		col = curCellEditInfo.getCellIndex();

		cellWidget = table.getWidget(row, col);

		table.setWidget(row, col, layoutTable);

		this.cellValue = cellValue;
	}

	/**
	 * @return the label text
	 */
	public String getLabel() {
		return layoutTable.getHTML(0, 0);
	}

	/**
	 * Set the label for this cell editor.
	 * 
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		layoutTable.setHTML(0, 0, label);
	}

	/**
	 * Accept the contents of the cell editor as the new cell value.
	 */
	protected void accept() {
		// Check if we are ready to accept
		if (!onAccept()) {
			return;
		}


		// Send the new cell value to the callback
		curCallback.onComplete(curCellEditInfo, cellValue);
		curCallback = null;
		curCellEditInfo = null;
		if (cellWidget != null)
			cellWidget.setVisible(true);
		cellWidget = null;
	}

	/**
	 * Cancel the cell edit.
	 */
	protected void cancel() {
		// Fire the event
		if (!onCancel()) {
			return;
		}

		// Call the callback
		if (curCallback != null) {
			curCallback.onCancel(curCellEditInfo);
			curCellEditInfo = null;
			curCallback = null;
		}
		cellWidget = null;

	}

	/**
	 * @return the Widget that is used to accept the current value.
	 */
	protected Widget getAcceptWidget() {
		ClickDecoratorPanel clickPanel = (ClickDecoratorPanel) layoutTable
				.getWidget(1, 1);
		return clickPanel.getWidget();
	}

	/**
	 * @return the Widget that is used to cancel editing.
	 */
	protected Widget getCancelWidget() {
		ClickDecoratorPanel clickPanel = (ClickDecoratorPanel) layoutTable
				.getWidget(1, 2);
		return clickPanel.getWidget();
	}

	/**
	 * @return the content widget
	 */
	protected Widget getContentWidget() {
		return layoutTable.getWidget(1, 0);
	}

	/**
	 * Get the additional number of pixels to offset this cell editor from the
	 * top left corner of the cell. Override this method to shift the editor
	 * left or right.
	 * 
	 * @return the additional left offset in pixels
	 */
	protected int getOffsetLeft() {
		return 0;
	}

	/**
	 * Get the additional number of pixels to offset this cell editor from the
	 * top left corner of the cell. Override this method to shift the editor up
	 * or down.
	 * 
	 * @return the additional top offset in pixels
	 */
	protected int getOffsetTop() {
		return 0;
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

	/**
	 * Set the Widget that is used to accept the current value.
	 * 
	 * @param w
	 *            the widget
	 */
	protected void setAcceptWidget(Widget w) {
		ClickDecoratorPanel clickPanel = new ClickDecoratorPanel(w,
				acceptHandler);
		clickPanel.setStyleName("accept");
		layoutTable.setWidget(1, 1, clickPanel);
	}

	/**
	 * Set the Widget that is used to cancel editing.
	 * 
	 * @param w
	 *            the widget
	 */
	protected void setCancelWidget(Widget w) {
		ClickDecoratorPanel clickPanel = new ClickDecoratorPanel(w,
				cancelHandler);
		clickPanel.setStyleName("cancel");
		layoutTable.setWidget(1, 2, clickPanel);
	}

}
