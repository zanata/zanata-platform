package net.openl10n.flies.resources;

import net.openl10n.flies.common.LocaleId;

import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;

public final class LocaleInputSourceTupple
{

   private final ImmutableList<LocaleId> localeIds;
   private final InputSource inputSource;

   public LocaleInputSourceTupple(InputSource inputSource, LocaleId... localeIds)
   {
      if (inputSource == null)
         throw new IllegalArgumentException("inputSource");
      this.inputSource = inputSource;

      for (int i = 0; i < localeIds.length; i++)
      {
         if (localeIds[i] == null)
            throw new IllegalArgumentException("localeIds");
      }
      this.localeIds = ImmutableList.of(localeIds);
   }

   public ImmutableList<LocaleId> getLocaleIds()
   {
      return localeIds;
   }

   public InputSource getInputSource()
   {
      return inputSource;
   }

}
