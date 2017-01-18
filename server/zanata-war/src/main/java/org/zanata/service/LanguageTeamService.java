package org.zanata.service;

import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;

import java.util.List;

public interface LanguageTeamService {
    final static int MAX_NUMBER_MEMBERSHIP = Integer.MAX_VALUE;

    List<HLocale> getLanguageMemberships(String userName);

    void joinOrUpdateRoleInLanguageTeam(String locale, Long personId,
            boolean isTranslator, boolean isReviewer, boolean isCoordinator)
            throws ZanataServiceException;

    boolean leaveLanguageTeam(String locale, Long personId);

    boolean isUserReviewer(Long personId);
}
