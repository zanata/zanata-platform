package net.openl10n.flies.resources;

import net.openl10n.flies.common.LocaleId;

import org.xml.sax.InputSource;

@Deprecated
public final class LocaleInputSourcePair
{

   private final LocaleId localeId;
   private final InputSource inputSource;

   public LocaleInputSourcePair(InputSource inputSource, LocaleId localeId)
   {
      if (inputSource == null)
         throw new IllegalArgumentException("inputSource");
      this.inputSource = inputSource;

      if (localeId == null)
         throw new IllegalArgumentException("localeId");
      this.localeId = localeId;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public InputSource getInputSource()
   {
      return inputSource;
   }

}
