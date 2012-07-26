package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.inject.ImplementedBy;

@ImplementedBy(SinglePageDataModelImpl.class)
public interface SinglePageDataModel
{
   void setSelected(int rowIndex);

   void setData(List<TransUnit> data);

   void update(TransUnit updatedTransUnit);

   TransUnit getSelectedOrNull();

   int getCurrentRow();

   boolean hasStaleData(ArrayList<String> newTargets);

   void addDataChangeListener(PageDataChangeListener pageDataChangeListener);

   int findIndexById(TransUnitId id);

   interface PageDataChangeListener
   {

      void showDataForCurrentPage(List<TransUnit> transUnits);

      void refreshView(int rowIndexOnPage, TransUnit updatedTransUnit);
   }
}
