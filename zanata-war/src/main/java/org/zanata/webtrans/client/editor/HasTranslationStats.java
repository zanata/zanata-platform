package org.zanata.webtrans.client.editor;

import org.zanata.common.TranslationStats;

public interface HasTranslationStats
{

   public static enum LabelFormat
   {
      PERCENT_COMPLETE, PERCENT_COMPLETE_HRS;

      public static final LabelFormat DEFAULT_FORMAT = PERCENT_COMPLETE_HRS;
   }

   public void setStats(TranslationStats stats);
}
