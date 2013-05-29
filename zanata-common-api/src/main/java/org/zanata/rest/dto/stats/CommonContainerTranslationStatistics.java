/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.dto.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CommonContainerTranslationStatistics implements Serializable
{
   private static final long serialVersionUID = 1L;
   private List<TranslationStatistics> stats;

   public CommonContainerTranslationStatistics()
   {
   }

   public List<TranslationStatistics> getStats()
   {
      return stats;
   }

   public void setStats(List<TranslationStatistics> stats)
   {
      this.stats = stats;
   }

   /**
    * Finds a specific translation for a locale and detail level.
    * 
    * @return The specified translation statistics element, or null if one
    *         cannot be found.
    */
   public TranslationStatistics getStats(String localeId, TranslationStatistics.StatUnit unit)
   {
      if (this.stats != null)
      {
         for (TranslationStatistics stat : this.stats)
         {
            if (stat.getLocale().equals(localeId) && stat.getUnit() == unit)
            {
               return stat;
            }
         }
      }
      return null;
   }

   public void addStats(TranslationStatistics newStats)
   {
      if (this.stats == null)
      {
         this.stats = new ArrayList<TranslationStatistics>();
      }
      this.stats.add(newStats);
   }

   public void set(CommonContainerTranslationStatistics otherContainerStatistics)
   {
      this.stats = otherContainerStatistics.getStats();
   }

   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder("CommonContainerTranslationStatistics{");
      sb.append("stats=").append(stats);
      sb.append('}');
      return sb.toString();
   }
}
