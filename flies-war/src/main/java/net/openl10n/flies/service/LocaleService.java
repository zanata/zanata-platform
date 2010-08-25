package net.openl10n.flies.service;

import java.util.List;
import java.util.Map;

import com.ibm.icu.util.ULocale;

import net.openl10n.flies.common.LocaleId;

public interface LocaleService
{
   Map<ULocale, LocaleId> getAllSupportedLanguages();

   void save(LocaleId localeId);

   void delete(LocaleId locale);

   List<LocaleId> getAllJavaLanguages();
}
