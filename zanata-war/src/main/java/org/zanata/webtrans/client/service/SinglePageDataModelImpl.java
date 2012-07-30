package org.zanata.webtrans.client.service;

import java.util.List;

import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.allen_sauer.gwt.log.client.Log;
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
   private final TransUnitNavigationService navigationService;
   private List<TransUnit> data = Lists.newArrayList();
   private int currentRow = -1;
   private PageDataChangeListener pageDataChangeListener;
   private int oldSelection = -2;

   @Inject
   public SinglePageDataModelImpl(EventBus eventBus, TransUnitNavigationService navigationService)
   {
      this.eventBus = eventBus;
      this.navigationService = navigationService;
   }

   @Override
   public void setSelected(int rowIndex)
   {
      Log.debug("current row:" + currentRow + " about to select row:" + rowIndex);
      if (currentRow != rowIndex)
      {
         oldSelection = currentRow;
         currentRow = rowIndex;
         navigationService.updateCurrentPageAndRowIndex(navigationService.getCurrentPage(), currentRow);
         eventBus.fireEvent(new TransUnitSelectionEvent(getSelectedOrNull()));
      }
   }

   @Override
   public void setData(List<TransUnit> data)
   {
      this.data = Lists.newArrayList(data);
      pageDataChangeListener.showDataForCurrentPage(this.data);
   }

   //for testing
   protected List<TransUnit> getData()
   {
      return data;
   }
   
   @Override
   public void updateIfInCurrentPage(TransUnit updatedTransUnit, EditorClientId editorClientId)
   {
      int index = findIndexById(updatedTransUnit.getId());
      if (validIndex(index))
      {
         data.set(index, updatedTransUnit);
         pageDataChangeListener.refreshView(index, updatedTransUnit, editorClientId);
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

   @Override
   public TransUnit getOldSelectionOrNull()
   {
      if (validIndex(oldSelection))
      {
         return data.get(oldSelection);
      }
      return null;
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
      return -1;
   }
}
