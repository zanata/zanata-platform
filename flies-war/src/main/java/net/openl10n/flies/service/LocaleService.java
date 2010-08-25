package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.FliesLocalePair;

public interface LocaleService
{
   List<FliesLocalePair> getAllSupportedLanguages();

   void save(LocaleId localeId);

   void delete(LocaleId locale);

   List<LocaleId> getAllJavaLanguages();
}
