package net.openl10n.flies.webtrans.client.editor.filter;

import net.openl10n.flies.webtrans.shared.model.TransUnit;

public class PhraseFilter implements ContentFilter<TransUnit>
{
   private String phrase;

   public PhraseFilter(String phrase)
   {
      this.phrase = phrase;
   }

   public static PhraseFilter from(String phrase)
   {
      return new PhraseFilter(phrase);
   }

   public String getPhrase()
   {
      return phrase;
   }

   public void setPhrase(String phrase)
   {
      this.phrase = phrase;
   }

   @Override
   public boolean accept(TransUnit value)
   {
      return value.getSource().toLowerCase().contains(phrase.toLowerCase()) || value.getTarget().toLowerCase().contains(phrase.toLowerCase());
   }

}
