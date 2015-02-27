/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.ui;

import java.util.List;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class to assist with the paging of data.
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractListFilter<T> {

    @Getter
    @Setter
    private int pageSize = 100;

    @Getter
    @Setter
    private int pageNumber = 1;

    @Getter
    @Setter
    private String filter;

    private List<T> currentPageData;

    private long totalRecords = -1L;

    public List<T> getCurrentPage() {
        if (currentPageData == null) {
            currentPageData = fetchRecords(getPageStartIdx(),
                    pageSize, filter);
        }
        return currentPageData;
    }

    public long getTotalRecords() {
        if (totalRecords < 0) {
            totalRecords = fetchTotalRecords(filter);
        }
        return totalRecords;
    }

    protected abstract List<T> fetchRecords(int start, int max, String filter);

    protected abstract long fetchTotalRecords(String filter);

    public boolean allowsNextPage() {
        return getTotalRecords() > pageNumber * pageSize;
    }

    public boolean allowsPreviousPage() {
        return pageNumber > 1;
    }

    public void firstPage() {
        pageNumber = 1;
        reset();
    }

    public void nextPage() {
        if(allowsNextPage()) {
            pageNumber++;
            reset();
        }
    }

    public void previousPage() {
        if(allowsPreviousPage()) {
            pageNumber--;
            reset();
        }
    }

    public int getPageStartIdx() {
        return (pageNumber-1) * pageSize;
    }

    public int getPageEndIdx() {
        return Math.min(getPageStartIdx() + pageSize, (int) getTotalRecords()) - 1;
    }

    public void reset() {
        currentPageData = null;
        totalRecords = -1L;
    }

    public void clearFilter() {
        filter = null;
    }
}
