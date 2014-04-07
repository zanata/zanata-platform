/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action.helper;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Helper class to assist with the paging of data.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see org.zanata.action.PagedListDataModel
 */
public abstract class PagedDataHandler<T> {

    @Getter
    @Setter
    private int pageSize = 10;

    @Getter
    @Setter
    private int pageNumber = 1;

    private List<T> currentPageData;

    private long totalRecords = -1L;

    public void nextPage() {
        if(allowsNextPage()) {
            pageNumber++;
            reset();
        }
    }

    public void prevPage() {
        if(allowsPrevPage()) {
            pageNumber--;
            reset();
        }
    }

    public long getCurrentPageFirstRecord() {
        return (pageNumber-1) * pageSize;
    }

    public boolean allowsNextPage() {
        return getTotalRecords() > pageNumber * pageSize;
    }

    public boolean allowsPrevPage() {
        return pageNumber > 1;
    }

    public List<T> getCurrentPageData() {
        if( currentPageData == null ) {
            currentPageData = fetchCurrentPage();
        }
        return currentPageData;
    }

    public long getTotalRecords() {
        if( totalRecords < 0 ) {
            totalRecords = fetchTotalRecords();
        }
        return totalRecords;
    }

    private void reset() {
        currentPageData = null;
        totalRecords = -1L;
    }

    protected abstract List<T> fetchCurrentPage();

    protected abstract long fetchTotalRecords();
}
