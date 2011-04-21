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
package org.zanata.action;

import java.util.List;

public class DataPage<E>
{
   private int datasetSize;

   private int startRow;

   private List<E> data;

   public DataPage(int datasetSize, int startRow, List<E> data)
   {

      this.datasetSize = datasetSize;

      this.startRow = startRow;

      this.data = data;

   }

   public int getDatasetSize()
   {

      return datasetSize;

   }

   public int getStartRow()
   {

      return startRow;

   }

   public List<E> getData()
   {

      return data;

   }

}
