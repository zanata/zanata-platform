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
package org.zanata.common;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

@Test(groups = { "unit-tests" })
public class TranslationStatsTest
{

   TranslationStats stats;
   TransUnitCount unitCount;
   TransUnitWords wordCount;

   @Test
   public void setGetUnitCount()
   {
      unitCount = new TransUnitCount();
      wordCount = new TransUnitWords();
      stats = new TranslationStats(unitCount, wordCount);
      
      assertThat(stats.getUnitCount(), sameInstance(unitCount));
   }

   @Test
   public void setGetWordCount()
   {
      unitCount = new TransUnitCount();
      wordCount = new TransUnitWords();
      stats = new TranslationStats(unitCount, wordCount);

      assertThat(stats.getWordCount(), sameInstance(wordCount));
   }

   @Test
   public void noArgConstructorSetsZeroStats()
   {
      stats = new TranslationStats();

      assertThat(stats.getUnitCount().getApproved(), is(0));
      assertThat(stats.getUnitCount().getNeedReview(), is(0));
      assertThat(stats.getUnitCount().getUntranslated(), is(0));
      assertThat(stats.getWordCount().getApproved(), is(0));
      assertThat(stats.getWordCount().getNeedReview(), is(0));
      assertThat(stats.getWordCount().getUntranslated(), is(0));
   }

   @Test
   public void setOverwritesValues()
   {
      unitCount = new TransUnitCount(5, 5, 5);
      wordCount = new TransUnitWords(5, 5, 5);
      stats = new TranslationStats(unitCount, wordCount);

      TranslationStats emptyStats = new TranslationStats();
      emptyStats.set(stats);

      assertThat(emptyStats.getUnitCount(), is(stats.getUnitCount()));
      assertThat(emptyStats.getWordCount(), is(stats.getWordCount()));
   }

   // TODO test these
   // public void add(TranslationStatsTest other)
   // {
   // unitCount.add(other.getUnitCount());
   // wordCount.add(other.getWordCount());
   // }
   //
   // public double getRemainingWordsHours()
   // {
   // return remainingHours(wordCount.getNeedReview(),
   // wordCount.getUntranslated());
   // }
   //
   // public int getApprovedPercent(boolean byWords)
   // {
   // if (byWords)
   // return wordCount.getApproved() * 100 / wordCount.getTotal();
   // else
   // return unitCount.getApproved() * 100 / unitCount.getTotal();
   // }
   //
   // private double remainingHours(int fuzzyWords, int untranslatedWords)
   // {
   // double untransHours = untranslatedWords / 250.0;
   // double fuzzyHours = fuzzyWords / 500.0;
   // double remainHours = untransHours + fuzzyHours;
   // return remainHours;
   // }

   @Test
   public void equals()
   {
      unitCount = new TransUnitCount(5, 5, 5);
      wordCount = new TransUnitWords(5, 5, 5);
      stats = new TranslationStats(unitCount, wordCount);

      TransUnitCount sameUnitCount = new TransUnitCount(5, 5, 5);
      TransUnitWords sameWordCount = new TransUnitWords(5, 5, 5);
      TranslationStats sameStats = new TranslationStats(sameUnitCount, sameWordCount);

      assertThat(stats, is(sameStats));

      TransUnitCount differentUnitCount = new TransUnitCount(2, 2, 2);
      TransUnitWords differentWordCount = new TransUnitWords(2, 2, 2);
      TranslationStats differentStats = new TranslationStats(differentUnitCount, differentWordCount);

      assertThat(stats, is(not(differentStats)));

      assertThat(stats, is(not(new Object())));
   }

}
