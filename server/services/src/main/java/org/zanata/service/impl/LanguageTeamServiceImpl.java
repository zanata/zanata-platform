package org.zanata.service.impl;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
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
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LanguageTeamService;
import javax.enterprise.event.Event;

@Named("languageTeamServiceImpl")
@RequestScoped
@Transactional
public class LanguageTeamServiceImpl implements LanguageTeamService {
    private static final long serialVersionUID = -1049037380252117557L;
    @Inject
    private PersonDAO personDAO;

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private LocaleMemberDAO localeMemberDAO;

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject
    private Event<LanguageTeamPermissionChangedEvent>
            languageTeamPermissionChangedEvent;


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
        if(isCoordinator) {
            isReviewer = true;
            isTranslator = true;
        }
        if (!alreadyJoined) {
            if (currentPerson.getLanguageMemberships().size() >= MAX_NUMBER_MEMBERSHIP) {
                throw new ZanataServiceException(
                        "You can only be a member of up to "
                                + MAX_NUMBER_MEMBERSHIP
                                + " languages at one time.");
            }
            HLocale lang = localeDAO.findByLocaleId(localeId);
            if (lang != null) {
                localeMember =
                        new HLocaleMember(currentPerson, lang, isTranslator,
                                isReviewer, isCoordinator);

                Set<HLocaleMember> members = lang.getMembers();
                if (members != null) {
                    members.add(localeMember);
                }
                permissionChangedEvent =
                        new LanguageTeamPermissionChangedEvent(currentPerson,
                                localeId, authenticatedUser)
                                .joiningTheTeam(isTranslator, isReviewer,
                                        isCoordinator);
                localeMemberDAO.makePersistent(localeMember);
                localeMemberDAO.flush();
                languageTeamPermissionChangedEvent.fire(permissionChangedEvent);
            }
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

            localeMemberDAO.makePersistent(localeMember);
            localeMemberDAO.flush();
            languageTeamPermissionChangedEvent.fire(permissionChangedEvent);
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
            Set<HLocaleMember> members = lang.getMembers();
            if (members != null) {
                members.remove(membership);
            }
            localeMemberDAO.flush();
            HPerson doneByPerson = authenticatedAccount.getPerson();
            languageTeamPermissionChangedEvent.fire(
                    new LanguageTeamPermissionChangedEvent(
                            currentPerson, lang.getLocaleId(),
                            doneByPerson)
                            .updatingPermissions(membership, false,
                                    false, false));
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
