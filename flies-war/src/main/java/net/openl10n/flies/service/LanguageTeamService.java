package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.exception.FliesException;
import net.openl10n.flies.model.HSupportedLanguage;

public interface LanguageTeamService
{
   static int MAX_NUMBER_MEMBERSHIP = Integer.MAX_VALUE;

   List<HSupportedLanguage> getLanguageMemberships(String userName);

   boolean joinLanguageTeam(String locale, Long personId) throws FliesException;

   boolean leaveLanguageTeam(String locale, Long personId);
}
