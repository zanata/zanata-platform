/*
 *
 *  * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
 *
 */

package org.zanata.webtrans.client.editor.table;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.shared.model.TransUnit;

import java.util.List;

@Singleton
public class PageNavigation implements HasPageNavigation
{
    private List<TransUnit> transUnits;
    private int currentPageIndex;
    private int pageCount;
    private int itemPerPage = 10;
    private TransUnitsModel model;
    private int totalCount;

    @Inject
    public PageNavigation(TransUnitsModel model) {
        this.model = model;
    }

    public void setItemPerPage(int itemPerPage) {
        this.itemPerPage = itemPerPage;
        pageCount = (int) Math.ceil(totalCount * 1.0 / itemPerPage);
    }

    public void setTransUnits(List<TransUnit> transUnits) {
        this.transUnits = ImmutableList.copyOf(transUnits);
        totalCount = transUnits.size();
        pageCount = (int) Math.ceil(totalCount * 1.0 / itemPerPage);
        gotoFirstPage();
    }

    @Override
    public void gotoFirstPage() {
        int endIndex = Math.min(transUnits.size(), itemPerPage);
        List<TransUnit> firstPageItems = transUnits.subList(0, endIndex);
        model.setTransUnits(firstPageItems);
        currentPageIndex = 0;
    }

    @Override
    public void gotoLastPage() {
        if (itemPerPage >= transUnits.size()) {
            model.setTransUnits(transUnits);
        } else {
            int startIndex = (pageCount - 1) * itemPerPage;
            List<TransUnit> lastPageItems = transUnits.subList(startIndex, totalCount);
            model.setTransUnits(lastPageItems);
            currentPageIndex = pageCount - 1;
        }
    }

    @Override
    public void gotoNextPage() {
        if (currentPageIndex < pageCount - 1) {
            currentPageIndex++;
            model.setTransUnits(getPageItems());
        }
    }

    private List<TransUnit> getPageItems() {
        int startIndex = currentPageIndex * itemPerPage;
        int endIndex = Math.min(startIndex + itemPerPage, totalCount);
        return transUnits.subList(startIndex, endIndex);
    }

    @Override
    public void gotoPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            model.setTransUnits(getPageItems());
        }
    }

    @Override
    public void gotoPage(int pageIndex, boolean forceReloadTable) {
        //force reload
        if (pageIndex > 0) {
            currentPageIndex = Math.min(pageIndex, pageCount - 1);
            model.setTransUnits(getPageItems());
        } else {
            gotoFirstPage();
        }
    }

    public TransUnitsModel getModel() {
        return model;
    }
    
    public int getCurrentPage() {
        return currentPageIndex + 1;
    }

    public int getPageCount() {
        return pageCount;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("totalCount", totalCount).
                add("pageCount", pageCount).
                add("itemPerPage", itemPerPage).
                add("currentPageIndex", currentPageIndex).
                toString();
    }
}
