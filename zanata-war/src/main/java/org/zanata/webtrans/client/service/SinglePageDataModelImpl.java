package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
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
   private final TargetContentsPresenter targetContentsPresenter;
   private final TransUnitSaveService transUnitSaveService;
   private List<TransUnit> data = Lists.newArrayList();
   private int currentRow = UNSELECTED;
   private PageDataChangeListener pageDataChangeListener;
   private int oldSelection = UNSELECTED;

   @Inject
   public SinglePageDataModelImpl(EventBus eventBus, TargetContentsPresenter targetContentsPresenter, TransUnitSaveService transUnitSaveService)
   {
      this.eventBus = eventBus;
      this.targetContentsPresenter = targetContentsPresenter;
      this.transUnitSaveService = transUnitSaveService;
   }

   @Override
   public void setSelected(int rowIndex)
   {
      //TODO change index to transUnitID?
      Log.debug("current row:" + currentRow + " about to select row:" + rowIndex);
      if (currentRow != rowIndex)
      {
         savePendingChangeIfApplicable();
         oldSelection = currentRow;
         currentRow = rowIndex;
         eventBus.fireEvent(new TransUnitSelectionEvent(getSelectedOrNull()));
      }
   }

   @Override
   public void savePendingChangeIfApplicable()
   {
      ArrayList<String> contentsInEditor = targetContentsPresenter.getNewTargets();
      TransUnit currentSelection = getSelectedOrNull();
      if (hasPendingChange(contentsInEditor, currentSelection))
      {
          transUnitSaveService.saveTranslation(currentSelection, contentsInEditor, currentSelection.getStatus(), new TransUnitSaveService.SaveResultCallback()
          {
             @Override
             public void onSaveSuccess(TransUnit updatedTU, UndoLink undoLink)
             {
                int rowIndex = findIndexById(updatedTU.getId());
                if (validIndex(rowIndex))
                {
                   targetContentsPresenter.addUndoLink(rowIndex, undoLink);
                }
             }

             @Override
             public void onSaveFail()
             {
             }
          });
      }
   }

   private boolean hasPendingChange(ArrayList<String> contentsInEditor, TransUnit currentSelection)
   {
      return currentSelection != null && contentsInEditor != null && !Objects.equal(currentSelection.getTargets(), contentsInEditor);
   }

   @Override
   public void setData(List<TransUnit> data)
   {
      //TODO we may be able to save pending change here??
      this.data = Lists.newArrayList(data);
      pageDataChangeListener.showDataForCurrentPage(this.data);
      currentRow = UNSELECTED;
      oldSelection = UNSELECTED;
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
      return UNSELECTED;
   }
}
