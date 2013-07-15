/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.presenter;

import java.util.Date;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ComparingPairTest
{
   private ComparingPair pair;

   @BeforeMethod
   public void setUp() throws Exception
   {
      pair = ComparingPair.empty();
   }

   private static TransHistoryItem newItem(String versionNum)
   {
      return new TransHistoryItem(versionNum, Lists.newArrayList("a"), ContentState.Approved, "", new Date());
   }

   @Test
   public void testAddWhenItsEmpty() throws Exception
   {
      TransHistoryItem newItem = newItem("1");
      pair = ComparingPair.empty().addOrRemove(newItem);

      assertThat(pair.one(), Matchers.sameInstance(newItem));
      assertThat(pair.two(), Matchers.nullValue());
   }

   @Test
   public void addSameItemTwiceWillRemoveIt()
   {
      TransHistoryItem newItem = newItem("1");
      pair = ComparingPair.empty().addOrRemove(newItem).addOrRemove(newItem);

      assertThat(pair.one(), Matchers.nullValue());
      assertThat(pair.two(), Matchers.nullValue());
   }

   @Test
   public void addSameItemToFullPairWillBeIgnored() throws Exception
   {
      TransHistoryItem one = newItem("1");
      TransHistoryItem two = newItem("2");
      TransHistoryItem three = newItem("3");
      pair = ComparingPair.empty().addOrRemove(one).addOrRemove(two);

      assertThat(pair.isFull(), Matchers.is(true));
      assertThat(pair.one(), Matchers.sameInstance(one));
      assertThat(pair.two(), Matchers.sameInstance(two));

      pair = pair.addOrRemove(three);

      assertThat(pair.isFull(), Matchers.is(true));
      assertThat(pair.one(), Matchers.sameInstance(one));
      assertThat(pair.two(), Matchers.sameInstance(two));
   }

   @Test
   public void testContains() throws Exception
   {
      TransHistoryItem one = newItem("1");
      TransHistoryItem two = newItem("2");
      pair = ComparingPair.empty().addOrRemove(one);

      assertThat(pair.contains(one), Matchers.is(true));
      assertThat(pair.contains(two), Matchers.is(false));
   }
}
