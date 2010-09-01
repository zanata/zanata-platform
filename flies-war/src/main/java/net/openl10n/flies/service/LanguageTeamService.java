package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.model.HSupportedLanguage;

public interface LanguageTeamService
{
   List<HSupportedLanguage> getLanguageMemberships(String userName);
}
