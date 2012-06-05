/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.Element;

public class InlineTargetCellEditor implements CellEditor<TransUnit>,
		TransUnitsEditModel {
	/**
	 * Default style name.
	 */
	public static final String DEFAULT_STYLENAME = "gwt-TargetCellEditor";

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

	private TransUnit cellValue;

	private boolean isOpened = false;
	private boolean isReadOnly;
	private TargetContentsPresenter targetContentsPresenter;

	private int curRow;
	private int curCol;
	private HTMLTable table;

	/**
	 * Construct a new {@link InlineTargetCellEditor} with the specified images.
	 */
	public InlineTargetCellEditor(CancelCallback<TransUnit> callback,
			EditRowCallback rowCallback, final boolean isReadOnly,
			TargetContentsPresenter targetContentsPresenter) {
		this.isReadOnly = isReadOnly;
		this.targetContentsPresenter = targetContentsPresenter;
		this.targetContentsPresenter.setCellEditor(this);

		cancelCallback = callback;
		editRowCallback = rowCallback;
	}

	private void gotoRow(NavigationType nav) {
		if (nav == NavigationType.NextEntry) {
			editRowCallback.gotoNextRow();
		} else if (nav == NavigationType.PrevEntry) {
			editRowCallback.gotoPrevRow();
		} else if (nav == NavigationType.FirstEntry) {
			editRowCallback.gotoFirstRow();
		} else if (nav == NavigationType.LastEntry) {
			editRowCallback.gotoLastRow();
		}
	}

	@Override
	public void gotoNewRow(NavigationType nav) {
		if (nav == NavigationType.NextEntry) {
			editRowCallback.gotoNextNewRow();
		} else if (nav == NavigationType.PrevEntry) {
			editRowCallback.gotoPrevNewRow();
		}
	}

	@Override
	public void gotoCurrentRow(boolean andEdit) {
		editRowCallback.gotoCurrentRow(andEdit);
	}

	@Override
	public void gotoFuzzyAndNewRow(NavigationType nav) {
		if (nav == NavigationType.NextEntry) {
			editRowCallback.gotoNextFuzzyNewRow();
		} else if (nav == NavigationType.PrevEntry) {
			editRowCallback.gotoPrevFuzzyNewRow();
		}
	}

	@Override
	public void gotoFuzzyRow(NavigationType nav) {
		if (nav == NavigationType.NextEntry) {
			editRowCallback.gotoNextFuzzyRow();
		} else if (nav == NavigationType.PrevEntry) {
			editRowCallback.gotoPrevFuzzyRow();
		}
	}

	public boolean isEditing() {
		return cellValue != null && targetContentsPresenter.isEditing();
	}

	public boolean isOpened() {
		return isOpened;
	}

	@Override
	public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue,
			Callback<TransUnit> callback) {
		if (isReadOnly) {
			return;
		}

		if (isEditing()) {
			if (cellEditInfo.getCellIndex() == curCol
					&& cellEditInfo.getRowIndex() == curRow) {
				return;
			}
		}

		// save the content in previous cell before start new editing
		if (curRow != cellEditInfo.getRowIndex()) {
			savePendingChange(false);
		}

		Log.debug("starting edit");

		// Save the current values
		curCallback = callback;
		curCellEditInfo = cellEditInfo;

		// Get the info about the cell
		table = curCellEditInfo.getTable();

		curRow = curCellEditInfo.getRowIndex();
		curCol = curCellEditInfo.getCellIndex();

		this.cellValue = cellValue;
		targetContentsPresenter.showEditors(
            TargetContentsPresenter.NO_OPEN_EDITOR);

		isOpened = true;

		Element element = table.getCellFormatter().getElement(curRow, curCol);
		TableEditorView.scrollIntoView(element);
	}

	@Override
	public void savePendingChange(boolean cancelIfUnchanged) {
		if (isEditing()) {
			// if something has changed, save as approved
			if (hasTargetContentsChanged()) {
				Log.debug("savePendingChange - acceptEdit");
				acceptEdit();
			}
		} else if (cancelIfUnchanged) {
			Log.debug("savePendingChange- cancel edit");
			cancelEdit();
		}
		targetContentsPresenter.setToViewMode();
	}

	public boolean hasTargetContentsChanged() {
		return !cellValue.getTargets().equals(
				targetContentsPresenter.getNewTargets());
	}

	@Override
	public void saveAndMoveRow(NavigationType nav) {
		savePendingChange(true);
		gotoRow(nav);
	}

	/**
	 * Accept the contents of the cell editor as the new cell value.
	 */
	@Override
	public void acceptEdit() {
		// Check if we are ready to accept
		if (!onAccept()) {
			return;
		}
		ArrayList<String> newTargets = targetContentsPresenter.getNewTargets();
		cellValue.setTargets(newTargets);
		determineStatus(newTargets, ContentState.Approved);

		targetContentsPresenter.setToViewMode();
		isOpened = false;

		// Send the new cell value to the callback
		curCallback.onComplete(curCellEditInfo, cellValue);
		clearSelection();
	}

	/**
	 * Save the contents of the cell and set status to fuzzy.
	 */
	@Override
	public void acceptFuzzyEdit() {
		// String text = textArea.getText();
		ArrayList<String> newTargets = targetContentsPresenter.getNewTargets();
		cellValue.setTargets(newTargets);
		determineStatus(newTargets, ContentState.NeedReview);
		curCallback.onComplete(curCellEditInfo, cellValue);
	}

   /**
    * 
    * @param newContents
    * @param requestedState
    * @see org.zanata.service.impl.TranslationServiceImpl#adjustContentState
    */
   private void determineStatus(ArrayList<String> newContents, ContentState requestedState)
   {
      int emptyCount = Iterables.size(Iterables.filter(newContents, new Predicate<String>()
      {
         @Override
         public boolean apply(@Nullable String input)
         {
            return Strings.isNullOrEmpty(input);
         }
      }));

      // TODO use ContentStateUtil.determineState.
      // ContentState stateToSet = ContentStateUtil.determineState(requestedState, newContents);

      // NB until then, make sure this stays consistent
		ContentState stateToSet = requestedState;
      if (requestedState == ContentState.New && emptyCount == 0)
      {
			stateToSet = ContentState.NeedReview;
		}
      else if (requestedState == ContentState.Approved && emptyCount != 0)
      {
         stateToSet = ContentState.New;
      }
      else if (requestedState == ContentState.NeedReview && emptyCount == newContents.size())
      {
			stateToSet = ContentState.New;
		}
		cellValue.setStatus(stateToSet);
	}

	/**
	 * Cancel the cell edit.
	 */
	public void cancelEdit() {
		// Fire the event
		if (!onCancel()) {
			return;
		}

		targetContentsPresenter.setToViewMode();
		isOpened = false;

		// Call the callback
		if (curCallback != null) {
			// curCallback.onCancel(curCellEditInfo);
			cancelCallback.onCancel(cellValue);
		}

		clearSelection();
	}

	public void clearSelection() {
		curCallback = null;
		curCellEditInfo = null;
		/*
		 * The main grid used for layout.
		 */
		cellValue = null;
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
	
	@Override
	public TransUnit getTargetCell() {
		return cellValue;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
		// cancelEdit();
	}

	public void showEditors(int rowIndex, int editorIndex) {
		targetContentsPresenter.showEditors(editorIndex);

	}

	@Override
	public int getCurrentRow() {

		return curRow;
	}

	@Override
	public void setRowValueOverride(int row, TransUnit targetCell) {
		editRowCallback.setRowValueOverride(row, targetCell);
	}
}
