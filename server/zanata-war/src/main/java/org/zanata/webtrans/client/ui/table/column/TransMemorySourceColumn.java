/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.ui.table.column;

import java.util.List;

import org.zanata.webtrans.client.ui.DiffMatchPatchLabel;
import org.zanata.webtrans.client.ui.table.cell.DiffMatchPatchLabelCell;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;

import com.google.gwt.user.cellview.client.Column;

public class TransMemorySourceColumn extends Column<TransMemoryResultItem, DiffMatchPatchLabel>
{
   private List<String> queries;

   public TransMemorySourceColumn()
   {
      super(new DiffMatchPatchLabelCell());
   }

   public void setQueries(List<String> queries)
   {
      this.queries = queries;
   }

   @Override
   public DiffMatchPatchLabel getValue(TransMemoryResultItem object)
   {
      DiffMatchPatchLabel label = new DiffMatchPatchLabel();
      // FIXME display multiple source/target strings
      label.setOriginal(queries.get(0));
      label.setText(object.getSource());
      return label;
   }

}
