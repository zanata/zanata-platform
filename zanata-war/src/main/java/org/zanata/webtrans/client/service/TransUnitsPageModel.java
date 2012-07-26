package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.presenter.TransUnitEditPresenter;
import org.zanata.webtrans.shared.model.TransUnit;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitsPageModel
{
   //TODO this class should listener to display selection change event
   private List<TransUnit> data = Lists.newArrayList();
   private int currentRow = 0;
   private TransUnitEditPresenter transUnitEditPresenter;


   public void setSelected(int row)
   {
      //TODO the view/presenter should call this on selection change
      currentRow = row;
   }

   public void setData(List<TransUnit> data)
   {
      this.data = Lists.newArrayList(data);
      transUnitEditPresenter.showData(this.data);
   }

   public List<TransUnit> getData()
   {
      return data;
   }
   
   public void update(TransUnit updatedTransUnit)
   {
      for (int rowNum = 0; rowNum < data.size(); rowNum++)
      {
         if (data.get(rowNum).getId().equals(updatedTransUnit.getId()))
         {
            data.set(rowNum, updatedTransUnit);
            //TODO here should notify view to refresh
         }
      }
   }

   public TransUnit getSelectedOrNull()
   {
      Preconditions.checkElementIndex(currentRow, data.size(), "current row index is invalid");
      return data.get(currentRow);
   }

   public int getCurrentRow()
   {
      return currentRow;
   }

   public boolean hasStaleData(ArrayList<String> newTargets)
   {
      TransUnit selectedOrNull = getSelectedOrNull();
      return selectedOrNull != null && !Objects.equal(selectedOrNull.getTargets(), newTargets);
   }

   public void addDataChangeListener(TransUnitEditPresenter transUnitEditPresenter)
   {
      this.transUnitEditPresenter = transUnitEditPresenter;
   }
}
