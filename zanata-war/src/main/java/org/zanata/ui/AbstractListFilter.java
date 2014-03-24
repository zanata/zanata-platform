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
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractListFilter<T> {

    @Getter
    private final static int countPerPage = 20;

    @Getter
    private int currentPage = 0;

    @Setter
    @Getter
    private String query;

    abstract protected List<T> getFilteredList();

    public int getFilteredListSize() {
        return getFilteredList().size();
    }

    public List<T> getPagedFilteredList() {
        List<List<T>> partition =
                Lists.partition(getFilteredList(), getCountPerPage());

        if (!partition.isEmpty() && partition.size() > getCurrentPage()) {
            return partition.get(getCurrentPage());
        }
        return Lists.newArrayList();
    };

    public String getListRange() {
        int total = getFilteredListSize();
        int upperBound = total == 0 ? 0 : (currentPage * countPerPage) + 1;
        int lowerBound = (currentPage + 1) * countPerPage;
        lowerBound = lowerBound > total ? total : lowerBound;
        if (upperBound == lowerBound) {
            return String.valueOf(upperBound);
        }
        return upperBound + "-" + lowerBound;
    }

    public void nextPage() {
        int totalPage =
                (int) Math.ceil((double) getFilteredListSize() / countPerPage) - 1;
        currentPage =
                (currentPage + 1) > totalPage ? totalPage : currentPage + 1;
    }

    public void previousPage() {
        currentPage = (currentPage - 1) < 0 ? 0 : currentPage - 1;
    }

    public void resetQueryAndPage() {
        resetPage();
        query = "";
    }

    public void resetPage() {
        currentPage = 0;
    }
}
