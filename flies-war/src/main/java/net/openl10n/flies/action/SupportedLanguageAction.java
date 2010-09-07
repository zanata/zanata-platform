package net.openl10n.flies.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.openl10n.flies.model.HSupportedLanguage;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("supportedLanguageAction")
@Scope(ScopeType.STATELESS)
public class SupportedLanguageAction
{
   @In
   LocaleService localeServiceImpl;

   static class TribeComparator implements Comparator<HSupportedLanguage>
   {
      public int compare(HSupportedLanguage aFliesLocale, HSupportedLanguage bFliesLocale)
      {
         String aDisplayName = aFliesLocale.retrieveDisplayName();
         String bDisplayName = bFliesLocale.retrieveDisplayName();
         int comparison = aDisplayName.compareTo(bDisplayName);
         if (comparison == 0)
         {
            String aNativeName = aFliesLocale.retrieveNativeName();
            String bNativeName = bFliesLocale.retrieveNativeName();
            comparison = aNativeName.compareTo(bNativeName);
            if (comparison == 0)
               // if all else fails, fall back on numerical ID sort
               return aFliesLocale.getLocaleId().getId().compareTo(bFliesLocale.getLocaleId().getId());
            return comparison;
         }
         return comparison;
      }
   }


   public List<HSupportedLanguage> getSupportedLanguages()
   {
      // NB ULocale data isn't stored in the database, so we have
      // to do a post-select sort.
      List<HSupportedLanguage> tribes = localeServiceImpl.getSupportedLocales();

      // This Comparator isn't complete enough for general use, but it should
      // work
      Collections.sort(tribes, new TribeComparator());
      return tribes;
   }


}
