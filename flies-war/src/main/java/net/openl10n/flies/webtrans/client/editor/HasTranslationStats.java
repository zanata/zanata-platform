package net.openl10n.flies.webtrans.client.editor;

import net.openl10n.flies.common.TranslationStats;

public interface HasTranslationStats
{

   public static enum LabelFormat
   {
      PERCENT_COMPLETE, HOURS_REMAIN, WORD_COUNTS, MESSAGE_COUNTS;
      public LabelFormat next()
      {
         return values()[(ordinal() + 1) % values().length];
      }
   }

   public void setStats(TranslationStats stats);

   public void setLabelFormat(LabelFormat format);

}
