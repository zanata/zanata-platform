/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package net.openl10n.flies.action;


import javax.faces.model.DataModel;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import net.openl10n.flies.exception.FliesServiceException;

public abstract class PagedListDataModel<E> extends DataModel
{
   private int DEFAULT_PAGESIZE = 15;
   private int pageSize = DEFAULT_PAGESIZE;
   private int rowIndex;
   private DataPage<E> page;
   protected static final LogProvider log = Logging.getLogProvider(PagedListDataModel.class);

   public PagedListDataModel()
   {
        super();
        this.rowIndex = -1;
        this.page = null;
    }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public int getPageSize()
   {
      return pageSize;
   }

   @SuppressWarnings("unchecked")
   public void setWrappedData(Object o)
   {
      if (o instanceof DataPage)
      {
         this.page = (DataPage<E>) o;
      }
      else
      {
         throw new FliesServiceException("Wrapped data class cast exception");
      }
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int index) {
        rowIndex = index;
    }

   public int getRowCount()
   {
       return getPage().getDatasetSize();
   }

   private DataPage<E> getPage()
   {
      if (page != null)
      {
         return page;
       }

      int rowIndex = getRowIndex();
      int startRow;
      if (rowIndex == -1)
      {
         startRow = 0;
      }
      else
      {
         startRow = rowIndex;
      }

      log.debug("start to fetch page from :" + startRow);
      page = fetchPage(startRow, pageSize);
      return page;
   }

   public Object getRowData()
   {
      if (rowIndex < 0)
      {
         throw new FliesServiceException("Invalid rowIndex" + rowIndex + "for PagedListDataModel");
      }
      if (page == null)
      {
         page = fetchPage(rowIndex, pageSize);
      }
      int datasetSize = page.getDatasetSize();
      int startRow = page.getStartRow();
      int count = page.getData().size();
      int endRow = startRow + count;
      if (rowIndex >= datasetSize)
      {
         throw new FliesServiceException("Invalid rowIndex" + rowIndex + "for PagedListDataModel");
       }
      if (rowIndex < startRow)
      {
           page = fetchPage(rowIndex, pageSize);
           startRow = page.getStartRow();
      }
      else if (rowIndex >= endRow)
      {
           page = fetchPage(rowIndex, pageSize);
           startRow = page.getStartRow();
      }
      return page.getData().get(rowIndex - startRow);
   }

   public Object getWrappedData() {
       return page.getData();
   }

   public boolean isRowAvailable() {
      DataPage<E> page = getPage();
       if (page == null) {
           return false;
       }
       int rowIndex = getRowIndex();
       if (rowIndex < 0) {
           return false;
       } else if (rowIndex >= page.getDatasetSize()) {
           return false;
       } else {
           return true;
       }
   }
   
   public abstract DataPage<E> fetchPage(int startRow, int pageSize);

   public void refresh()
   {
      if (this.page != null) {
          this.page = null;
          getPage();
      }
   }

}
