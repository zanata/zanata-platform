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

   public List<FliesLocalePair> getAllLocales()
   {
      List<FliesLocalePair> supportedLanguage = new ArrayList<FliesLocalePair>();
      List<HSupportedLanguage> hSupportedLanguages = supportedLanguageDAO.findAll();
      if (hSupportedLanguages == null)
         return supportedLanguage;
      for (HSupportedLanguage hSupportedLanguage : hSupportedLanguages)
      {
         FliesLocalePair fliesLocalePair = new FliesLocalePair(hSupportedLanguage);
         supportedLanguage.add(fliesLocalePair);
      }
      return supportedLanguage;
   }

   public void save(LocaleId localeId)
   {
      if (localeExists(localeId))
         return;
      HSupportedLanguage entity = new HSupportedLanguage();
      entity.setLocaleId(localeId);
      entity.setActive(true);
      supportedLanguageDAO.makePersistent(entity);
      supportedLanguageDAO.flush();
   }

   public void disable(HSupportedLanguage entity)
   {
      if (entity != null)
      {
         entity.setActive(false);
         supportedLanguageDAO.makePersistent(entity);
         supportedLanguageDAO.flush();
      }
   }

   public List<LocaleId> getAllJavaLanguages()
   {
      ULocale[] locales = ULocale.getAvailableLocales();
      List<LocaleId> addedLocales = new ArrayList<LocaleId>();
      for (ULocale locale : locales)
      {
         LocaleId localeId = new FliesLocalePair(locale).gethSupportedLanguage().getLocaleId();
         addedLocales.add(localeId);
      }
      return addedLocales;
   }

   public void enable(HSupportedLanguage entity)
   {
      if (entity != null)
      {
         entity.setActive(true);
         supportedLanguageDAO.makePersistent(entity);
         supportedLanguageDAO.flush();
      }
   }
   
   public boolean localeExists(LocaleId locale)
   {
      HSupportedLanguage entity = supportedLanguageDAO.findByLocaleId(locale);
      return entity != null;
   }
   
   public List<FliesLocalePair> getSupportedLocales()
   {
      List<FliesLocalePair> supportedLanguage = new ArrayList<FliesLocalePair>();
      List<HSupportedLanguage> hSupportedLanguages = supportedLanguageDAO.findAllActive();
      if (hSupportedLanguages == null)
         return supportedLanguage;
      for (HSupportedLanguage hSupportedLanguage : hSupportedLanguages)
      {
         FliesLocalePair fliesLocalePair = new FliesLocalePair(hSupportedLanguage);
         supportedLanguage.add(fliesLocalePair);
      }
      return supportedLanguage;
   }

   public boolean localeSupported(LocaleId locale)
   {
      HSupportedLanguage entity = supportedLanguageDAO.findByLocaleId(locale);
      return entity != null && entity.isActive();
   }
}
