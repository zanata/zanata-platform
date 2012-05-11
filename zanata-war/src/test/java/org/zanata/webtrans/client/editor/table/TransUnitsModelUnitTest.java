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
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.TestFixture;
import org.zanata.webtrans.shared.model.TransUnit;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class TransUnitsModelUnitTest {
    public static final List<TransUnit> TRANS_UNITS = makeTransUnits();
    private TransUnitsModel model =  new TransUnitsModel();
    
    private static List<TransUnit> makeTransUnits() {
        return Lists.newArrayList(
                TestFixture.makeTransUnit(0, ContentState.New),
                TestFixture.makeTransUnit(1, ContentState.New),
                TestFixture.makeTransUnit(2, ContentState.NeedReview),
                TestFixture.makeTransUnit(3, ContentState.Approved),
                TestFixture.makeTransUnit(4, ContentState.NeedReview),
                TestFixture.makeTransUnit(5, ContentState.New)
        );
    }

    @BeforeMethod
    public void setUp() {
        model.setTransUnits(TRANS_UNITS);
    }

    public void canGoToFirstRow() {
        model.gotoFirstRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));
    }

    public void canGoToLastRow() {
        model.gotoLastRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(TRANS_UNITS.size() - 1)));
    }

    public void canGoToNextRow() {

        model.gotoNextRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(1)));
        model.gotoNextRow();//2
        model.gotoNextRow();//3
        model.gotoNextRow();//4
        model.gotoNextRow();//5
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
        model.gotoNextRow();//5
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
    }

    public void canGoToNextFuzzyNewRow() {
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));

        model.gotoNextFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(1)));

        model.gotoNextFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(2)));

        model.gotoNextFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(4)));

        model.gotoNextFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));

        model.gotoNextFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
    }

    public void canGoToPreviousFuzzyNewRow() {
        model.gotoLastRow();

        model.gotoPrevFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(4)));

        model.gotoPrevFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(2)));

        model.gotoPrevFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(1)));

        model.gotoPrevFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));

        model.gotoPrevFuzzyNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));
    }

    public void canGoToNextFuzzyRow() {
        model.gotoNextFuzzyRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(2)));

        model.gotoNextFuzzyRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(4)));
        model.gotoNextFuzzyRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(4)));
    }

    public void canGoToPreviousFuzzyRow() {
        model.gotoLastRow();

        model.gotoPrevFuzzyRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(4)));

        model.gotoPrevFuzzyRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(2)));
        model.gotoPrevFuzzyRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(2)));
    }

    public void canGoToNextNewRow() {
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));

        model.gotoNextNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(1)));

        model.gotoNextNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
        model.gotoNextNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
    }

    public void canGoToPreviousNewRow() {
        model.gotoLastRow();

        model.gotoPrevNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(1)));

        model.gotoPrevNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));
        model.gotoPrevNewRow();
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(0)));
    }

    public void canMoveToArbitraryTransUnit() {
        boolean moved = model.moveToIndex(5);
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
        assertThat(moved, Matchers.equalTo(true));
        //won't move
        moved = model.moveToIndex(6);
        assertThat(model.getCurrentTransUnit(), Matchers.equalTo(TRANS_UNITS.get(5)));
        assertThat(moved, Matchers.equalTo(false));

    }
}
