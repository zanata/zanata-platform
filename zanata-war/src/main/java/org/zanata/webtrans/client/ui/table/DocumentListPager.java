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
package org.zanata.webtrans.client.ui.table;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.Range;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class DocumentListPager extends SimplePager
{

   public DocumentListPager(TextLocation location, boolean showFastForwardButton, boolean showLastPageButton)
   {
      super(location, showFastForwardButton, showLastPageButton);
      this.setRangeLimited(true);
   }

   /**
    * Overriding setPageStart@AbstractPager. Only shows remaining rows in the
    * last page when isRangeLimited = true
    * 
    */
   @Override
   public void setPageStart(int index)
   {
      if (this.getDisplay() != null)
      {
         Range range = getDisplay().getVisibleRange();
         int pageSize = range.getLength();
         if (!isRangeLimited() && getDisplay().isRowCountExact())
         {
            index = Math.min(index, getDisplay().getRowCount() - pageSize);
         }
         index = Math.max(0, index);
         if (index != range.getStart())
         {
            getDisplay().setVisibleRange(index, pageSize);
         }
      }
   }

}
