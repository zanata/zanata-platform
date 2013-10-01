/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.ui;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.component.SortOrder;
import org.richfaces.component.UIColumn;

/**
 * Handles column sorting in a page. Used in conjuction with the
 * richext:columnSorter custom composite tag to recreate automatic sorting in
 * rich:dataTable components.
 *
 * This might not be needed once richfaces' data table is updated to support
 * this feature.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see columnSorter.xhtml
 */
@Name("columnSort")
@Scope(ScopeType.PAGE)
@AutoCreate
public class ColumnSort {

    private Map<String, SortOrder> sortingOrder =
            new HashMap<String, SortOrder>();

    public SortOrder getSorting(String colName) {
        SortOrder sort = sortingOrder.get(colName);
        if (sort == null) {
            sort = SortOrder.unsorted;
        }

        return sort;
    }

    public void cycleSorting(ColumnSortCompositeComponent columnSort) {
        UIColumn column = columnSort.getParentColumn();
        SortOrder currentSort = getSorting(column.getId());
        SortOrder newSort = SortOrder.unsorted;

        if (currentSort == null || currentSort == SortOrder.unsorted
                || currentSort == SortOrder.descending) {
            newSort = SortOrder.ascending;
        } else if (currentSort == SortOrder.ascending) {
            newSort = SortOrder.descending;
        }

        for (UIColumn col : columnSort.getTableColumns()) {
            // Clicked column gets the new sort order
            if (col.getId().equals(column.getId())) {
                col.setSortOrder(newSort);
                sortingOrder.put(col.getId(), newSort);
            }
            // Other columns get unsorted
            else {
                col.setSortOrder(SortOrder.unsorted);
                sortingOrder.put(col.getId(), SortOrder.unsorted);
            }
        }

    }

    public boolean isUnsorted(String column) {
        return getSorting(column) == SortOrder.unsorted;
    }

    public boolean isSortedAscending(String column) {
        return getSorting(column) == SortOrder.ascending;
    }

    public boolean isSortedDescending(String column) {
        return getSorting(column) == SortOrder.descending;
    }

}
