package org.zanata.webtrans.client.service;

import java.util.List;

import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class SinglePageDataModelImpl
{
   private List<TransUnit> data = Lists.newArrayList();
   private int currentRow = NavigationController.UNSELECTED;

   protected void setSelected(int rowIndex)
   {
      Log.info("current row:" + currentRow + " about to select row:" + rowIndex);
      currentRow = rowIndex;
   }

   protected TransUnit getByIdOrNull(TransUnitId transUnitId)
   {
      int indexById = findIndexById(transUnitId);
      if (validIndex(indexById))
      {
         return data.get(indexById);
      }
      return null;
   }

   protected void setData(List<TransUnit> data)
   {
      this.data = Lists.newArrayList(data);
      currentRow = NavigationController.UNSELECTED;
   }

   protected List<TransUnit> getData()
   {
      return data;
   }

   /**
    * update data if it's in current data list.
    *
    * @param updatedTransUnit updated trans unit
    * @return true if the updatedTransUnit is in current data list. False if it's not.
    */
   protected boolean updateIfInCurrentPage(TransUnit updatedTransUnit)
   {
      int index = findIndexById(updatedTransUnit.getId());
      if (validIndex(index))
      {
         data.set(index, updatedTransUnit);
         return true;
      }
      return false;
   }

   protected TransUnit getSelectedOrNull()
   {
      if (validIndex(currentRow))
      {
         return data.get(currentRow);
      }
      return null;
   }

   protected int getCurrentRow()
   {
      return currentRow;
   }

   private boolean validIndex(int rowIndex)
   {
      return rowIndex >= 0 && rowIndex < data.size();
   }

   protected int findIndexById(TransUnitId id)
   {
      for (int rowNum = 0; rowNum < data.size(); rowNum++)
      {
         if (data.get(rowNum).getId().equals(id))
         {
            return rowNum;
         }
      }
      return NavigationController.UNSELECTED;
   }
}
