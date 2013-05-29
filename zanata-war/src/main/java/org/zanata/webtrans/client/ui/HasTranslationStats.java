package org.zanata.webtrans.client.ui;

import org.zanata.rest.dto.stats.CommonContainerTranslationStatistics;

public interface HasTranslationStats
{
   public static enum LabelFormat
   {
      PERCENT_COMPLETE, PERCENT_COMPLETE_HRS;
   }

   void setStats(CommonContainerTranslationStatistics stats, boolean statsByWords);
}
