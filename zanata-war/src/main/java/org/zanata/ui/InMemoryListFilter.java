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
package org.zanata.ui;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Implementation of a list filter that keeps its records in memory.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class InMemoryListFilter<T> extends AbstractListFilter<T> {

    private List<T> elements;

    @Override
    protected long fetchTotalRecords(String filter) {
        loadElements();
        return elements.size();
    }

    @Override
    protected List<T> fetchRecords(int start, int max, final String filter) {
        loadElements();
        List<T> filteredList = Lists.newArrayList(elements);
        if( filter != null ) {
            filteredList = Lists.newArrayList(
                    Collections2.filter(elements, new Predicate<T>() {
                        @Override
                        public boolean apply(T input) {
                            return include(input, filter);
                        }
                    })
            );
        }
        return filteredList
                    .subList(start, Math.min(start + max, filteredList.size()));
    }

    private void loadElements() {
        if(elements == null) {
            elements = fetchAll();
        }
    }

    /**
     * Fetches all records.
     * @return A list of all records to be managed by the filter.
     */
    protected abstract List<T> fetchAll();

    /**
     * Indicates whether the element should be included in the results.
     * 
     * @param elem
     *            The element to analyze
     * @param filter
     *            The filter string being used.
     * @return True if the element passes the filter. False otherwise.
     */
    protected abstract boolean include(T elem, String filter);

    @Override
    public void reset() {
        super.reset();
        elements = null;
    }
}
