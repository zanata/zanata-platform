package net.openl10n.flies.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.LocaleDAO;
import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.model.HLocale;
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
   private LocaleDAO supportedLanguageDAO;
   
   @In
   public void setLocaleDAO(LocaleDAO supportedLanguageDAO)
   {
      this.supportedLanguageDAO= supportedLanguageDAO;
   }

   public List<FliesLocalePair> getAllLocales()
   {
      List<FliesLocalePair> supportedLanguage = new ArrayList<FliesLocalePair>();
      List<HLocale> hSupportedLanguages = supportedLanguageDAO.findAll();
      if (hSupportedLanguages == null)
         return supportedLanguage;
      for (HLocale hSupportedLanguage : hSupportedLanguages)
      {
         FliesLocalePair fliesLocalePair = new FliesLocalePair(hSupportedLanguage.getLocaleId());
         fliesLocalePair.setActive(hSupportedLanguage.isActive());
         supportedLanguage.add(fliesLocalePair);
      }
      return supportedLanguage;
   }

   public void save(LocaleId localeId)
   {
      if (localeExists(localeId))
         return;
      HLocale entity = new HLocale();
      entity.setLocaleId(localeId);
      entity.setActive(true);
      supportedLanguageDAO.makePersistent(entity);
      supportedLanguageDAO.flush();
   }

   public void disable(LocaleId localeId)
   {
      HLocale entity = supportedLanguageDAO.findByLocaleId(localeId);
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
         LocaleId localeId = new FliesLocalePair(locale).getLocaleId();
         addedLocales.add(localeId);
      }
      return addedLocales;
   }

   public void enable(LocaleId localeId)
   {
      HLocale entity = supportedLanguageDAO.findByLocaleId(localeId);
      if (entity != null)
      {
         entity.setActive(true);
         supportedLanguageDAO.makePersistent(entity);
         supportedLanguageDAO.flush();
      }
   }
   
   public boolean localeExists(LocaleId locale)
   {
      HLocale entity = supportedLanguageDAO.findByLocaleId(locale);
      return entity != null;
   }
   
   public List<HLocale> getSupportedLocales()
   {
      return supportedLanguageDAO.findAllActive();
   }

   public boolean localeSupported(LocaleId locale)
   {
      HLocale entity = supportedLanguageDAO.findByLocaleId(locale);
      return entity != null && entity.isActive();
   }

   public HLocale getSupportedLanguageByLocale(LocaleId locale) throws FliesServiceException
   {

      HLocale hLocale = supportedLanguageDAO.findByLocaleId(locale);
      if (hLocale == null || !hLocale.isActive())
      {
         throw new FliesServiceException("Unsupported Locale: " + locale.getId() + " within this context");
      }
      return hLocale;
   }

   @Deprecated
   @Override
   public HLocale getDefautLanguage()
   {
      HLocale de = null;
      try
      {
         de = getSupportedLanguageByLocale(LocaleId.EN_US);
      }
      catch (FliesServiceException e)
      {
         de = new HLocale(LocaleId.EN_US);
      }
      return de;
   }
}
