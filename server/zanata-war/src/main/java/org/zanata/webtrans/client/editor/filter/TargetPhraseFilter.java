package org.zanata.webtrans.client.editor.filter;

import org.zanata.webtrans.shared.model.TransUnit;

public class TargetPhraseFilter implements ContentFilter<TransUnit>
{

   private String phrase;

   public TargetPhraseFilter(String phrase)
   {
      this.phrase = phrase;
   }

   @Override
   public boolean accept(TransUnit value)
   {
      return value.getTarget().contains(phrase);
   }

   public static TargetPhraseFilter from(String phrase)
   {
      return new TargetPhraseFilter(phrase);
   }

   public String getPhrase()
   {
      return phrase;
   }

   public void setPhrase(String phrase)
   {
      this.phrase = phrase;
   }
}
