package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
//TODO move methods accessed by TransUnitEditPresenter to NavigationController so that this class can become package private
public class SinglePageDataModelImpl implements SinglePageDataModel
{
   private final EventBus eventBus;
   private List<TransUnit> data = Lists.newArrayList();
   private int currentRow = UNSELECTED;
   private PageDataChangeListener pageDataChangeListener;

   @Inject
   public SinglePageDataModelImpl(EventBus eventBus)
   {
      this.eventBus = eventBus;
   }

   @Override
   public void setSelected(int rowIndex)
   {
      //TODO change index to transUnitID?
      Log.info("current row:" + currentRow + " about to select row:" + rowIndex);
      if (currentRow != rowIndex)
      {
         currentRow = rowIndex;
         eventBus.fireEvent(new TransUnitSelectionEvent(getSelectedOrNull()));
      }
   }

   @Override
   public TransUnit getByIdOrNull(TransUnitId transUnitId)
   {
      int indexById = findIndexById(transUnitId);
      if (validIndex(indexById))
      {
         return data.get(indexById);
      }
      return null;
   }

   @Override
   public void setData(List<TransUnit> data)
   {
      this.data = Lists.newArrayList(data);
      pageDataChangeListener.showDataForCurrentPage(this.data);
      currentRow = UNSELECTED;
   }

   @Override
   public List<TransUnit> getData()
   {
      return data;
   }
   
   @Override
   public void updateIfInCurrentPage(TransUnit updatedTransUnit, EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType)
   {
      int index = findIndexById(updatedTransUnit.getId());
      if (validIndex(index))
      {
         data.set(index, updatedTransUnit);
         pageDataChangeListener.refreshView(index, updatedTransUnit, editorClientId, updateType);
      }
   }

   @Override
   public TransUnit getSelectedOrNull()
   {
      if (validIndex(currentRow))
      {
         return data.get(currentRow);
      }
      return null;
   }

   @Override
   public int getCurrentRow()
   {
      return currentRow;
   }

   @Override
   public void addDataChangeListener(PageDataChangeListener pageDataChangeListener)
   {
      this.pageDataChangeListener = pageDataChangeListener;
   }

   private boolean validIndex(int rowIndex)
   {
      return rowIndex >= 0 && rowIndex < data.size();
   }

   @Override
   public int findIndexById(TransUnitId id)
   {
      for (int rowNum = 0; rowNum < data.size(); rowNum++)
      {
         if (data.get(rowNum).getId().equals(id))
         {
            return rowNum;
         }
      }
      return UNSELECTED;
   }
}
