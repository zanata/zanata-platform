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

import java.io.Serializable;
import javax.faces.model.DataModel;
import org.zanata.exception.ZanataServiceException;

public abstract class PagedListDataModel<E> extends DataModel
        implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PagedListDataModel.class);
    private static final long serialVersionUID = 1L;
    private int DEFAULT_PAGESIZE = 15;
    private int pageSize = DEFAULT_PAGESIZE;
    private int rowIndex;
    private int scrollerPage = 1;
    private DataPage<E> page;

    public PagedListDataModel() {
        this.rowIndex = -1;
        this.page = null;
    }

    @SuppressWarnings("unchecked")
    public void setWrappedData(Object o) {
        if (o instanceof DataPage) {
            this.page = (DataPage<E>) o;
        } else {
            throw new ZanataServiceException(
                    "Wrapped data class cast exception");
        }
    }

    public int getRowCount() {
        return getPage().getDatasetSize();
    }

    private DataPage<E> getPage() {
        if (page != null) {
            return page;
        }
        int rowIndex = getRowIndex();
        int startRow;
        if (rowIndex == -1) {
            startRow = 0;
        } else {
            startRow = rowIndex;
        }
        log.debug("start to fetch page from :" + startRow);
        page = fetchPage(startRow, pageSize);
        return page;
    }

    public Object getRowData() {
        if (rowIndex < 0) {
            throw new IllegalArgumentException(
                    "Invalid rowIndex (< 0): " + rowIndex);
        }
        boolean alreadyFetched;
        if (page == null) {
            page = fetchPage(rowIndex, pageSize);
            alreadyFetched = true;
        } else {
            alreadyFetched = false;
        }
        int datasetSize = page.getDatasetSize();
        int pageStartRow = page.getStartRow();
        int pageRowCount = page.getData().size();
        // actually it's end row plus one
        int pageEndRow = pageStartRow + pageRowCount;
        if (rowIndex >= datasetSize) {
            throw new IllegalArgumentException(
                    "Invalid rowIndex (>= dataSetSize): " + rowIndex);
        }
        if (rowIndex < pageStartRow || rowIndex >= pageEndRow) {
            if (alreadyFetched) {
                throw new RuntimeException(
                        "Fetched page range [" + pageStartRow + "," + pageEndRow
                                + ") does not include rowIndex " + rowIndex);
            } else {
                page = fetchPage(rowIndex, pageSize);
                pageStartRow = page.getStartRow();
            }
        }
        assert pageStartRow <= rowIndex && rowIndex < pageEndRow;
        return page.getData().get(rowIndex - pageStartRow);
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

    public void refresh() {
        if (this.page != null) {
            this.page = null;
            getPage();
        }
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public int getRowIndex() {
        return this.rowIndex;
    }

    public void setRowIndex(final int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getScrollerPage() {
        return this.scrollerPage;
    }

    public void setScrollerPage(final int scrollerPage) {
        this.scrollerPage = scrollerPage;
    }
}
