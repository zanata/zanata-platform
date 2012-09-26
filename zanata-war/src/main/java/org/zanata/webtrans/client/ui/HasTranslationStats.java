package org.zanata.webtrans.client.ui;

import org.zanata.common.TranslationStats;

public interface HasTranslationStats
{
   public static enum LabelFormat
   {
      PERCENT_COMPLETE, PERCENT_COMPLETE_HRS;
   }

   void setStats(TranslationStats stats, boolean statsByWords);
}
