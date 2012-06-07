/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zanata.webtrans.client.presenter.TransUnitEditPresenter;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitProvidesKey;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/

/**
 * This class is used as data provider to translation unit celltable display as well as defining selection model of it.
 */
@Singleton
public class TransUnitsDataModel extends ListDataProvider<TransUnit>
{

   private final SingleSelectionModel<TransUnit> selectionModel;

   public TransUnitsDataModel()
   {
      super(TransUnitProvidesKey.KEY_PROVIDER);
      selectionModel = new SingleSelectionModel<TransUnit>(TransUnitProvidesKey.KEY_PROVIDER);
   }

   //for testing
   TransUnitsDataModel(SingleSelectionModel<TransUnit> selectionModel)
   {
      super(TransUnitProvidesKey.KEY_PROVIDER);
      this.selectionModel = selectionModel;
   }

   public SelectionModel<TransUnit> getSelectionModel()
   {
      return selectionModel;
   }

   public TransUnit getSelectedOrNull()
   {
      return selectionModel.getSelectedObject();
   }

   public HandlerRegistration addSelectionChangeHandler(SelectionChangeEvent.Handler handler)
   {
      return selectionModel.addSelectionChangeHandler(handler);
   }

   public void selectById(TransUnitId transUnitId)
   {
      Log.info("select by trans unit id: " + transUnitId);
      List<TransUnit> units = getList();
      Collection<TransUnit> found = Collections2.filter(units, new FindByTransUnitIdPredicate(transUnitId));
      if (found.size() == 1)
      {
         TransUnit toBeSelected = found.iterator().next();
         selectionModel.setSelected(toBeSelected, true);
      }
      else
      {
         Log.warn("can not find and select transUnit with id: " + transUnitId);
      }
   }

   public void selectByRowNumber(int gotoRow)
   {
      Log.info("select by row number: " + gotoRow);
      Preconditions.checkElementIndex(gotoRow, getList().size(), "go to row");
      TransUnit toBeSelected = getList().get(gotoRow);
      selectionModel.setSelected(toBeSelected, true);
   }

   public boolean update(TransUnit updatedTU)
   {
      for (int i = 0; i < getList().size(); i++)
      {
         TransUnit unit = getList().get(i);
         if (unit.getId().equals(updatedTU.getId()))
         {
            Log.info("update TU at row " + i);
            getList().set(i, updatedTU);
            return true;
         }
      }
      Log.warn("can not find TU to update. updated TU id:" + updatedTU.getId());
      return false;
   }
}
