package org.zanata.service;

import java.io.Serializable;
import java.util.List;

import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;

public interface LanguageTeamService extends Serializable {
    final static int MAX_NUMBER_MEMBERSHIP = Integer.MAX_VALUE;

    List<HLocale> getLanguageMemberships(String userName);

    void joinOrUpdateRoleInLanguageTeam(String locale, Long personId,
            boolean isTranslator, boolean isReviewer, boolean isCoordinator)
            throws ZanataServiceException;

    boolean leaveLanguageTeam(String locale, Long personId);

    boolean isUserReviewer(Long personId);
}
