package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.inject.ImplementedBy;

@ImplementedBy(SinglePageDataModelImpl.class)
public interface SinglePageDataModel
{
   int UNSELECTED = -1;

   void setSelected(int rowIndex);

   void setData(List<TransUnit> data);

   void updateIfInCurrentPage(TransUnit updatedTransUnit, EditorClientId editorClientId);

   TransUnit getSelectedOrNull();

   int getCurrentRow();

   void addDataChangeListener(PageDataChangeListener pageDataChangeListener);

   int findIndexById(TransUnitId id);

   void savePendingChangeIfApplicable(ArrayList<String> newTargets);

   TransUnit getByIdOrNull(TransUnitId transUnitId);

   List<TransUnit> getData();

   interface PageDataChangeListener
   {

      void showDataForCurrentPage(List<TransUnit> transUnits);

      void refreshView(int rowIndexOnPage, TransUnit updatedTransUnit, EditorClientId editorClientId);
   }
}
