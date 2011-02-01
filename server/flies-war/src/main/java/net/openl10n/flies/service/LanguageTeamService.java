package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.HLocale;

public interface LanguageTeamService
{
   final static int MAX_NUMBER_MEMBERSHIP = Integer.MAX_VALUE;

   List<HLocale> getLanguageMemberships(String userName);

   boolean joinLanguageTeam(String locale, Long personId) throws FliesServiceException;

   boolean leaveLanguageTeam(String locale, Long personId);
}
