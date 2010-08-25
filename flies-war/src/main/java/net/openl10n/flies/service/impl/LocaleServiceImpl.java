package net.openl10n.flies.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.SupportedLanguageDAO;
import net.openl10n.flies.model.HSupportedLanguage;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.util.LocaleUtil;

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

   public Map<ULocale, LocaleId> getAllSupportedLanguages()
   {
      Map<ULocale, LocaleId> supportedLanguage = new HashMap<ULocale, LocaleId>();
      List<HSupportedLanguage> hSupportedLanguages = supportedLanguageDAO.findAll();
      if (hSupportedLanguages == null)
         return supportedLanguage;
      for (HSupportedLanguage hSupportedLanguage : hSupportedLanguages)
      {
         supportedLanguage.put(new ULocale(hSupportedLanguage.getLocaleId().getId()), hSupportedLanguage.getLocaleId());
         log.debug("get supported languages from table:" + hSupportedLanguage.getLocaleId());
      }
      return supportedLanguage;
   }

   public void save(LocaleId localeId)
   {
      HSupportedLanguage entity = new HSupportedLanguage();
      entity.setLocaleId(localeId);
      log.debug("save locale:" + localeId.getId());
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
      log.debug("add localeId...");
      for (ULocale locale : locales)
      {
         LocaleId localeId = LocaleUtil.toLocaleId(locale);
         addedLocales.add(localeId);
      }
      return addedLocales;
   }

}
