package net.openl10n.flies.webtrans.client.editor;

import net.openl10n.flies.common.TranslationStats;

public interface HasTranslationStats
{

   public static enum LabelFormat
   {
      WORD_COUNTS, HOURS_REMAIN, PERCENT_COMPLETE, MESSAGE_COUNTS;
      public LabelFormat next()
      {
         return values()[(ordinal() + 1) % values().length];
      }
      public static final LabelFormat DEFAULT_FORMAT = WORD_COUNTS;
   }

   public void setStats(TranslationStats stats);

   public void setLabelFormat(LabelFormat format);

}
