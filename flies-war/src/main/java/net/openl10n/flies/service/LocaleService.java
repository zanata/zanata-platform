package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.model.HSupportedLanguage;

public interface LocaleService
{
   List<FliesLocalePair> getAllLocales();

   void save(LocaleId localeId);

   void disable(HSupportedLanguage locale);

   void enable(HSupportedLanguage locale);

   List<LocaleId> getAllJavaLanguages();

   boolean localeExists(LocaleId locale);
   
   boolean localeSupported(LocaleId locale);

   List<FliesLocalePair> getSupportedLocales();
}
