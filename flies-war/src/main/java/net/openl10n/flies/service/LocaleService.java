package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.exception.FliesException;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.model.HLocale;

public interface LocaleService
{
   List<FliesLocalePair> getAllLocales();

   void save(LocaleId localeId);

   void disable(LocaleId locale);

   void enable(LocaleId locale);

   List<LocaleId> getAllJavaLanguages();

   boolean localeExists(LocaleId locale);
   
   List<HLocale> getSupportedLocales();

   HLocale getSupportedLanguageByLocale(LocaleId locale) throws FliesException;

   @Deprecated
   HLocale getDefautLanguage();
}
