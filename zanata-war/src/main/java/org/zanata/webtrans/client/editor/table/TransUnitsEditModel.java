package org.zanata.webtrans.client.editor.table;

import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.shared.model.TransUnit;

/**
 * This is mainly to abstract some methods out from InlineTargetCellEditor. So
 * that other newly created class won't have to depend on ScrollPagingTable
 * classes.
 * TODO to be removed
 */
public interface TransUnitsEditModel {

	void acceptFuzzyEdit();

	TransUnit getTargetCell();

	void gotoNewRow(NavTransUnitEvent.NavigationType nav);

	void gotoFuzzyAndNewRow(NavTransUnitEvent.NavigationType nav);

	void gotoFuzzyRow(NavTransUnitEvent.NavigationType nav);

	void savePendingChange(boolean cancelIfUnchanged);

	void saveAndMoveRow(NavTransUnitEvent.NavigationType nav);

	void acceptEdit();

	void gotoCurrentRow(boolean andEdit);

	int getCurrentRow();

	void setRowValueOverride(int row, TransUnit targetCell);
}
