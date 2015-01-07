package org.zanata.service.impl;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HLocaleMember.HLocaleMemberPk;
import org.zanata.model.HPerson;
import org.zanata.service.LanguageTeamService;

@Name("languageTeamServiceImpl")
@Scope(ScopeType.STATELESS)
public class LanguageTeamServiceImpl implements LanguageTeamService {
    private PersonDAO personDAO;

    private LocaleDAO localeDAO;

    private LocaleMemberDAO localeMemberDAO;

    private HAccount authenticatedAccount;

    @In
    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    @In
    public void setLocaleDAO(LocaleDAO localeDAO) {
        this.localeDAO = localeDAO;
    }

    @In
    public void setLocaleMemberDAO(LocaleMemberDAO localeMemberDAO) {
        this.localeMemberDAO = localeMemberDAO;
    }

    @In(value = JpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION)
    public void setAuthenticatedAccount(HAccount authenticatedAccount) {
        this.authenticatedAccount = authenticatedAccount;
    }


    public List<HLocale> getLanguageMemberships(String userName) {
        return personDAO.getLanguageMembershipByUsername(userName);
    }

    public void joinOrUpdateRoleInLanguageTeam(String locale, Long personId,
            boolean isTranslator, boolean isReviewer, boolean isCoordinator)
            throws ZanataServiceException {
        LocaleId localeId = new LocaleId(locale);
        HPerson currentPerson = personDAO.findById(personId, false);

        boolean alreadyJoined =
                localeMemberDAO.isLocaleMember(personId, localeId);
        HLocaleMember localeMember;
        LanguageTeamPermissionChangedEvent permissionChangedEvent;

        HPerson authenticatedUser = authenticatedAccount.getPerson();
        if (!alreadyJoined) {
            if (currentPerson.getLanguageMemberships().size() >= MAX_NUMBER_MEMBERSHIP) {
                throw new ZanataServiceException(
                        "You can only be a member of up to "
                                + MAX_NUMBER_MEMBERSHIP
                                + " languages at one time.");
            }
            HLocale lang = localeDAO.findByLocaleId(localeId);
            localeMember =
                    new HLocaleMember(currentPerson, lang, isTranslator,
                            isReviewer, isCoordinator);
            lang.getMembers().add(localeMember);
            permissionChangedEvent =
                    new LanguageTeamPermissionChangedEvent(currentPerson,
                            localeId, authenticatedUser)
                            .joiningTheTeam(isTranslator, isReviewer,
                                    isCoordinator);
        } else {
            localeMember =
                    localeMemberDAO.findByPersonAndLocale(personId, localeId);
            permissionChangedEvent =
                    new LanguageTeamPermissionChangedEvent(currentPerson,
                            localeId, authenticatedUser)
                            .updatingPermissions(localeMember,
                            isTranslator, isReviewer, isCoordinator);
            localeMember.setTranslator(isTranslator);
            localeMember.setReviewer(isReviewer);
            localeMember.setCoordinator(isCoordinator);
        }
        localeMemberDAO.makePersistent(localeMember);
        localeMemberDAO.flush();
        if (Events.exists()) {
            Events.instance().raiseTransactionSuccessEvent(
                    LanguageTeamPermissionChangedEvent.LANGUAGE_TEAM_PERMISSION_CHANGED,
                    permissionChangedEvent);
        }
    }

    public boolean leaveLanguageTeam(String locale, Long personId) {
        HLocale lang = localeDAO.findByLocaleId(new LocaleId(locale));
        HPerson currentPerson = personDAO.findById(personId, false);
        final HLocaleMember membership =
                localeMemberDAO.findById(new HLocaleMemberPk(currentPerson,
                        lang), true);

        if (membership != null) {
            localeMemberDAO.makeTransient(membership);
            lang.getMembers().remove(membership);
            localeMemberDAO.flush();
            if (Events.exists()) {
                HPerson doneByPerson = authenticatedAccount.getPerson();
                Events.instance()
                        .raiseTransactionSuccessEvent(
                                LanguageTeamPermissionChangedEvent.LANGUAGE_TEAM_PERMISSION_CHANGED,
                                new LanguageTeamPermissionChangedEvent(
                                        currentPerson, lang.getLocaleId(),
                                        doneByPerson)
                                        .updatingPermissions(membership, false,
                                                false, false));
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isUserReviewer(Long personId) {
        return !localeMemberDAO.findByPersonWithReviewerRole(personId)
                .isEmpty();
    }
}
