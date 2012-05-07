package org.zanata.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;

@Name("supportedLanguageAction")
@Scope(ScopeType.STATELESS)
public class SupportedLanguageAction
{
   @In
   private LocaleService localeServiceImpl;

   static class TribeComparator implements Comparator<HLocale>
   {
      public int compare(HLocale aZanataLocale, HLocale bZanataLocale)
      {
         String aDisplayName = aZanataLocale.retrieveDisplayName();
         String bDisplayName = bZanataLocale.retrieveDisplayName();
         int comparison = aDisplayName.compareTo(bDisplayName);
         if (comparison == 0)
         {
            String aNativeName = aZanataLocale.retrieveNativeName();
            String bNativeName = bZanataLocale.retrieveNativeName();
            comparison = aNativeName.compareTo(bNativeName);
            if (comparison == 0)
               // if all else fails, fall back on numerical ID sort
               return aZanataLocale.getLocaleId().getId().compareTo(bZanataLocale.getLocaleId().getId());
            return comparison;
         }
         return comparison;
      }
   }


   public List<HLocale> getSupportedLanguages()
   {
      // NB ULocale data isn't stored in the database, so we have
      // to do a post-select sort.
      List<HLocale> tribes = localeServiceImpl.getSupportedLocales();

      // This Comparator isn't complete enough for general use, but it should
      // work
      Collections.sort(tribes, new TribeComparator());
      return tribes;
   }


}
