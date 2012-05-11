/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.editor.table;

import com.google.common.collect.Lists;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.TestFixture;
import org.zanata.webtrans.shared.model.TransUnit;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@Test
public class PageNavigationUnitTest {
    private static final List<TransUnit> SIX_TRANS_UNITS = makeTransUnits();
    public static final boolean FORCE_RELOAD_TABLE = false;
    private PageNavigation page;
    private TransUnitsModel model;

    private static List<TransUnit> makeTransUnits() {
        List<TransUnit> transUnits = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            transUnits.add(TestFixture.makeTransUnit(i));
        }
        return transUnits;
    }

    @BeforeMethod
    public void setUp() throws Exception {
        model = new TransUnitsModel();
        page = new PageNavigation(model);
        page.setTransUnits(SIX_TRANS_UNITS);
    }

    public void canGoToFirstPage() {
        page.setItemPerPage(3);
        page.gotoFirstPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(0), SIX_TRANS_UNITS.get(1), SIX_TRANS_UNITS.get(2)));

        page.setItemPerPage(7);
        page.gotoFirstPage();
        assertThat(model.getTransUnits(), equalTo(SIX_TRANS_UNITS));
    }

    public void canGoToLastPage() {
        page.setItemPerPage(3);
        page.gotoLastPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(3), SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));

        page.setItemPerPage(4);
        page.gotoLastPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));

        page.setItemPerPage(7);
        page.gotoLastPage();
        assertThat(model.getTransUnits(), equalTo(SIX_TRANS_UNITS));
    }

    public void canGoToNextPage() {
        page.setItemPerPage(3);
        page.gotoNextPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(3), SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));
        page.gotoNextPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(3), SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));

        page.setItemPerPage(4);
        page.gotoNextPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));
    }

    public void canGoToPreviousPage() {
        page.setItemPerPage(3);
        page.gotoPreviousPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(0), SIX_TRANS_UNITS.get(1), SIX_TRANS_UNITS.get(2)));
        page.gotoLastPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(3), SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));

        page.gotoPreviousPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(0), SIX_TRANS_UNITS.get(1), SIX_TRANS_UNITS.get(2)));
        page.gotoPreviousPage();
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(0), SIX_TRANS_UNITS.get(1), SIX_TRANS_UNITS.get(2)));
    }
    
    public void canGoToPage() {
        page.setItemPerPage(3);
        page.gotoPage(1, FORCE_RELOAD_TABLE);
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(3), SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));
        page.gotoPage(5, FORCE_RELOAD_TABLE);
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(3), SIX_TRANS_UNITS.get(4), SIX_TRANS_UNITS.get(5)));

        page.gotoPage(0, FORCE_RELOAD_TABLE);
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(0), SIX_TRANS_UNITS.get(1), SIX_TRANS_UNITS.get(2)));
        page.gotoPage(-1, FORCE_RELOAD_TABLE);
        assertThat(model.getTransUnits(), hasItems(SIX_TRANS_UNITS.get(0), SIX_TRANS_UNITS.get(1), SIX_TRANS_UNITS.get(2)));
    }
}
