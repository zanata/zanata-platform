package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;

import com.google.gwt.user.cellview.client.TextColumn;

public class SimilarityColumn extends TextColumn<TranslationMemoryGlossaryItem>
{
   @Override
   public String getValue(TranslationMemoryGlossaryItem object)
   {
      return object.getSimilarityPercent() + "%";
   }
}
