package net.openl10n.flies.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("languageTeamAction")
@Scope(ScopeType.STATELESS)
public class LanguageTeamAction
{
   @In
   LocaleService localeServiceImpl;

   static class TribeComparator implements Comparator<FliesLocalePair>
   {
      public int compare(FliesLocalePair aFliesLocale, FliesLocalePair bFliesLocale)
      {
         String aDisplayName = aFliesLocale.getuLocale().getDisplayName();
         String bDisplayName = bFliesLocale.getuLocale().getDisplayName();
         int comparison = aDisplayName.compareTo(bDisplayName);
         if (comparison == 0)
         {
            String aNativeName = aFliesLocale.getuLocale().getDisplayName(aFliesLocale.getuLocale());
            String bNativeName = bFliesLocale.getuLocale().getDisplayName(bFliesLocale.getuLocale());
            comparison = aNativeName.compareTo(bNativeName);
            if (comparison == 0)
               // if all else fails, fall back on numerical ID sort
               return aFliesLocale.gethSupportedLanguage().getLocaleId().getId().compareTo(bFliesLocale.gethSupportedLanguage().getLocaleId().getId());
            return comparison;
         }
         return comparison;
      }
   }


   public List<FliesLocalePair> getSupportedLanguages()
   {
      // NB ULocale data isn't stored in the database, so we have
      // to do a post-select sort.
      List<FliesLocalePair> tribes = localeServiceImpl.getSupportedLocales();

      // This Comparator isn't complete enough for general use, but it should
      // work
      Collections.sort(tribes, new TribeComparator());
      return tribes;
   }

}
