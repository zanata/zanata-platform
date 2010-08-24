package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.common.LocaleId;

public interface LocaleService
{
   List<LocaleId> getAllSupportedLanguages();

   void save(LocaleId localeId);

   void delete(LocaleId localeId);

   List<LocaleId> getAllJavaLanguages();
}
