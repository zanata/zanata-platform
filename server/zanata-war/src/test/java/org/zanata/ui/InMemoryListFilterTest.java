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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class InMemoryListFilterTest {

    private InMemoryListFilter<Integer> listFilter;

    @Before
    public void prepareData() {
        final List<Integer> elements =
                Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                        13, 14, 15, 16, 17, 18, 19, 20);
        listFilter = new InMemoryListFilter<Integer>() {
            @Override
            protected List<Integer> fetchAll() {
                return elements;
            }

            @Override
            protected boolean include(Integer elem, String filter) {
                return elem.toString().contains(filter);
            }
        };
        listFilter.setPageSize(10);
    }

    @Test
    public void pageNavigation() throws Exception {
        // Page 1 (First page)
        assertThat(listFilter.allowsNextPage()).isTrue();
        assertThat(listFilter.allowsPreviousPage()).isFalse();
        assertThat(listFilter.getPageNumber()).isEqualTo(1);

        listFilter.nextPage(); // Move to page 2
        assertThat(listFilter.getPageNumber()).isEqualTo(2);
        assertThat(listFilter.allowsNextPage()).isTrue();
        assertThat(listFilter.allowsPreviousPage()).isTrue();

        listFilter.nextPage(); // Move to page 3
        assertThat(listFilter.getPageNumber()).isEqualTo(3);
        assertThat(listFilter.allowsNextPage()).isFalse();
        assertThat(listFilter.allowsPreviousPage()).isTrue();

        listFilter.previousPage(); // Back to page 2
        assertThat(listFilter.getPageNumber()).isEqualTo(2);

        listFilter.previousPage(); // Back to page 1
        assertThat(listFilter.getPageNumber()).isEqualTo(1);
    }

    @Test
    public void pageContents() throws Exception {
        // Page 1
        assertThat(listFilter.getCurrentPage()).contains(0, 1, 2, 3, 4, 5, 6, 7,
                8, 9);

        // Page 2
        listFilter.nextPage();
        assertThat(listFilter.getCurrentPage()).contains(10, 11, 12, 13, 14, 15,
                16, 17, 18, 19);

        // Page 3
        listFilter.nextPage();
        assertThat(listFilter.getCurrentPage()).contains(20);
    }

    @Test
    public void testFiltering() throws Exception {
        listFilter.setFilter("1");
        assertThat(listFilter.getCurrentPage()).contains(1, 11, 12, 13, 14, 15,
                16, 17, 18);
        assertThat(listFilter.allowsNextPage()).isTrue();

        listFilter.nextPage();
        assertThat(listFilter.getCurrentPage()).contains(19);
        assertThat(listFilter.allowsNextPage()).isFalse();
    }

    @Test
    public void firstPage() throws Exception {
        listFilter.setPageNumber(3);
        listFilter.firstPage();
        assertThat(listFilter.allowsPreviousPage()).isFalse();
        assertThat(listFilter.getPageNumber()).isEqualTo(1);
    }

    @Test
    public void pageIndexes() throws Exception {
        // Page 1
        assertThat(listFilter.getPageStartIdx()).isEqualTo(0);
        assertThat(listFilter.getPageEndIdx()).isEqualTo(9);

        // Page 2
        listFilter.nextPage();
        assertThat(listFilter.getPageStartIdx()).isEqualTo(10);
        assertThat(listFilter.getPageEndIdx()).isEqualTo(19);

        // Page 3
        listFilter.nextPage();
        assertThat(listFilter.getPageStartIdx()).isEqualTo(20);
        assertThat(listFilter.getPageEndIdx()).isEqualTo(20);
    }

    @Test
    public void clearFilter() throws Exception {
        listFilter.setFilter("won't find anything");
        assertThat(listFilter.getCurrentPage()).isEmpty();

        listFilter.clearFilter();
        assertThat(listFilter.getFilter()).isNullOrEmpty();
        listFilter.reset();
        assertThat(listFilter.getPageNumber()).isEqualTo(1);
        assertThat(listFilter.getCurrentPage().size()).isEqualTo(10);
    }

    @Test
    public void pageSize() throws Exception {
        listFilter.setPageSize(5);
        assertThat(listFilter.getPageSize()).isEqualTo(5);

        // Page 1
        assertThat(listFilter.getCurrentPage().size()).isEqualTo(5);
        assertThat(listFilter.getPageStartIdx()).isEqualTo(0);
        assertThat(listFilter.getPageEndIdx()).isEqualTo(4);

        // Page 2
        listFilter.nextPage();
        assertThat(listFilter.getPageStartIdx()).isEqualTo(5);
        assertThat(listFilter.getPageEndIdx()).isEqualTo(9);
    }
}
