package net.openl10n.flies.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.SupportedLanguageDAO;
import net.openl10n.flies.model.HSupportedLanguage;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

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
   @Logger
   private Log log;

   public List<LocaleId> getAllSupportedLanguages()
   {
      List<LocaleId> supportedLanguage = new ArrayList<LocaleId>();
      List<HSupportedLanguage> hSupportedLanguages = supportedLanguageDAO.findAll();
      if (hSupportedLanguages == null)
         supportedLanguage = new ArrayList<LocaleId>();
      for (HSupportedLanguage hSupportedLanguage : hSupportedLanguages)
      {
         supportedLanguage.add(hSupportedLanguage.getLocaleId());
      }
      return supportedLanguage;
   }

   public void save(LocaleId localeId)
   {
      HSupportedLanguage entity = new HSupportedLanguage();
      entity.setLocaleId(localeId);
      supportedLanguageDAO.makePersistent(entity);
   }

   public void delete(LocaleId localeId)
   {
      HSupportedLanguage entity = supportedLanguageDAO.findById(localeId, true);
      supportedLanguageDAO.makeTransient(entity);
   }

   public List<LocaleId> getAllJavaLanguages()
   {
      ULocale[] locales = ULocale.getAvailableLocales();
      List<LocaleId> addedLocales = new ArrayList<LocaleId>();
      log.debug("add localeId...");
      for (ULocale locale : locales)
      {
         LocaleId localeId = new LocaleId(locale);
         addedLocales.add(localeId);
      }
      return addedLocales;
   }

}
