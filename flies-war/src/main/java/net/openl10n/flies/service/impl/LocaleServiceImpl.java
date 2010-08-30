package net.openl10n.flies.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.SupportedLanguageDAO;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.model.HSupportedLanguage;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.ibm.icu.util.ULocale;

/**
 * This implementation provides all the business logic related to Locale.
 * 
 */
@Name("localeServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class LocaleServiceImpl implements LocaleService
{
   @In
   SupportedLanguageDAO supportedLanguageDAO;

   public List<FliesLocalePair> getAllSupportedLanguages()
   {
      List<FliesLocalePair> supportedLanguage = new ArrayList<FliesLocalePair>();
      List<HSupportedLanguage> hSupportedLanguages = supportedLanguageDAO.findAll();
      if (hSupportedLanguages == null)
         return supportedLanguage;
      for (HSupportedLanguage hSupportedLanguage : hSupportedLanguages)
      {
         supportedLanguage.add(new FliesLocalePair(hSupportedLanguage.getLocaleId()));
      }
      return supportedLanguage;
   }

   public void save(LocaleId localeId)
   {
      HSupportedLanguage entity = new HSupportedLanguage();
      entity.setLocaleId(localeId);
      supportedLanguageDAO.makePersistent(entity);
   }

   public void delete(LocaleId locale)
   {
      HSupportedLanguage entity = supportedLanguageDAO.findById(locale, true);
      supportedLanguageDAO.makeTransient(entity);
   }

   public List<LocaleId> getAllJavaLanguages()
   {
      ULocale[] locales = ULocale.getAvailableLocales();
      List<LocaleId> addedLocales = new ArrayList<LocaleId>();
      for (ULocale locale : locales)
      {
         LocaleId localeId = new FliesLocalePair(locale).getLocaleId();
         addedLocales.add(localeId);
      }
      return addedLocales;
   }

}
