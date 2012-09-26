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

import java.io.Serializable;

public class TranslationStats implements Serializable
{
   private static final long serialVersionUID = 1L;
   private TransUnitCount unitCount;
   private TransUnitWords wordCount;

   public TranslationStats()
   {
      this(new TransUnitCount(), new TransUnitWords());
   }

   public TranslationStats(TransUnitCount unitCount, TransUnitWords wordCount)
   {
      this.unitCount = unitCount;
      this.wordCount = wordCount;
   }

   public TransUnitCount getUnitCount()
   {
      return unitCount;
   }

   public TransUnitWords getWordCount()
   {
      return wordCount;
   }

   public void set(TranslationStats count)
   {
      this.unitCount = count.unitCount;
      this.wordCount = count.wordCount;
   }

   public void add(TranslationStats other)
   {
      unitCount.add(other.getUnitCount());
      wordCount.add(other.getWordCount());
   }

   public double getRemainingWordsHours()
   {
      return remainingHours(wordCount.getNeedReview(), wordCount.getUntranslated());
   }

   public double getRemainingMsgHours()
   {
      return remainingHours(unitCount.getNeedReview(), unitCount.getUntranslated());
   }

   public int getApprovedPercent(boolean byWords)
   {
      if (byWords)
      {
         if (wordCount.getTotal() == 0)
         {
            return 0;
         }
         return wordCount.getApproved() * 100 / wordCount.getTotal();
      }
      else
      {
         if (unitCount.getTotal() == 0)
         {
            return 0;
         }
         return unitCount.getApproved() * 100 / unitCount.getTotal();
      }
   }

   private double remainingHours(int fuzzy, int untranslated)
   {
      double untransHours = untranslated / 250.0;
      double fuzzyHours = fuzzy / 500.0;
      double remainHours = untransHours + fuzzyHours;
      return remainHours;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null)
         return false;
      if (obj instanceof TranslationStats)
      {
         TranslationStats o = (TranslationStats) obj;
         return (unitCount.equals(o.unitCount) && wordCount.equals(o.wordCount));
      }
      return false;
   }

}
