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

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;

@Test
public class TransUnitsModelUnitTest
{
   private final Map<Long, ContentState> transIdStateList = new HashMap<Long, ContentState>();
   private final ArrayList<Long> idIndexList = new ArrayList<Long>();

   private TransUnitsModel model = new TransUnitsModel();

   @BeforeMethod
   public void setUp()
   {
      transIdStateList.put(new Long(0), ContentState.New);
      transIdStateList.put(new Long(1), ContentState.New);
      transIdStateList.put(new Long(2), ContentState.NeedReview);
      transIdStateList.put(new Long(3), ContentState.Approved);
      transIdStateList.put(new Long(4), ContentState.NeedReview);
      transIdStateList.put(new Long(5), ContentState.New);

      idIndexList.add(new Long(0));
      idIndexList.add(new Long(1));
      idIndexList.add(new Long(2));
      idIndexList.add(new Long(3));
      idIndexList.add(new Long(4));
      idIndexList.add(new Long(5));

      model.init(transIdStateList, idIndexList);
   }

   public void canGoToFirstRow()
   {
      model.gotoFirstRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));
   }

   public void canGoToLastRow()
   {
      model.gotoLastRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(idIndexList.size() - 1));
   }

   public void canGoToNextRow()
   {
      model.gotoNextRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(1));
      model.gotoNextRow();// 2
      model.gotoNextRow();// 3
      model.gotoNextRow();// 4
      model.gotoNextRow();// 5
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));
   }

   public void canGoToNextFuzzyNewRow()
   {
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));

      model.gotoNextFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(1));

      model.gotoNextFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(2));

      model.gotoNextFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(4));

      model.gotoNextFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));

      model.gotoNextFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));
   }

   public void canGoToPreviousFuzzyNewRow()
   {
      model.gotoLastRow();

      model.gotoPrevFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(4));

      model.gotoPrevFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(2));

      model.gotoPrevFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(1));

      model.gotoPrevFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));

      model.gotoPrevFuzzyNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));
   }

   public void canGoToNextFuzzyRow()
   {
      model.gotoNextFuzzyRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(2));

      model.gotoNextFuzzyRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(4));
      model.gotoNextFuzzyRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(4));
   }

   public void canGoToPreviousFuzzyRow()
   {
      model.gotoLastRow();

      model.gotoPrevFuzzyRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(4));

      model.gotoPrevFuzzyRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(2));
      model.gotoPrevFuzzyRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(2));
   }

   public void canGoToNextNewRow()
   {
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));

      model.gotoNextNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(1));

      model.gotoNextNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));
      model.gotoNextNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));
   }

   public void canGoToPreviousNewRow()
   {
      model.gotoLastRow();

      model.gotoPrevNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(1));

      model.gotoPrevNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));
      model.gotoPrevNewRow();
      assertThat(model.getCurrentIndex(), Matchers.equalTo(0));
   }

   public void canMoveToArbitraryTransUnit()
   {
      boolean moved = model.moveToIndex(5);
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));
      assertThat(moved, Matchers.equalTo(true));
      // won't move
      moved = model.moveToIndex(6);
      assertThat(model.getCurrentIndex(), Matchers.equalTo(5));
      assertThat(moved, Matchers.equalTo(false));

   }
}
