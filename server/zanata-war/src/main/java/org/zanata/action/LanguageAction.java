/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.security.annotations.Authenticated;
import org.zanata.model.LanguageRequest;
import org.zanata.model.LocaleRole;
import org.zanata.security.annotations.CheckRole;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.events.JoinedLanguageTeam;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.events.LeftLanguageTeam;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;
import org.zanata.service.RequestService;
import org.zanata.ui.faces.FacesMessages;
import javax.enterprise.event.Event;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.util.UrlUtil;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Named("languageAction")
@ViewScoped
@Model
@Transactional
public class LanguageAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LanguageAction.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private LanguageTeamService languageTeamServiceImpl;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private PersonDAO personDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private Messages msgs;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private Event<JoinedLanguageTeam> joinLanguageTeamEvent;
    @Inject
    private Event<LanguageTeamPermissionChangedEvent> languageTeamPermissionChangedEvent;
    @Inject
    private Event<LeftLanguageTeam> leaveLanguageTeamEvent;
    @Inject
    private LocaleMemberDAO localeMemberDAO;
    @Inject
    private RequestService requestServiceImpl;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private UrlUtil urlUtil;
    private String language;
    private String searchTerm;
    private HLocale locale;
    private List<SelectablePerson> searchResults;
    private AbstractListFilter<HLocaleMember> membersFilter =
            new InMemoryListFilter<HLocaleMember>() {

                @Override
                protected List<HLocaleMember> fetchAll() {
                    return localeMemberDAO
                            .findAllByLocale(new LocaleId(language));
                }

                @Override
                protected boolean include(HLocaleMember elem, String filter) {
                    return StringUtils.containsIgnoreCase(
                            elem.getPerson().getName(), filter);
                }
            };

    public List<LanguageRequest> getRequests() {
        if (identity == null) {
            return Lists.newArrayList();
        }
        if (identity != null
                && identity.hasPermission(locale, "manage-language-team")) {
            return requestServiceImpl
                    .getPendingLanguageRequests(locale.getLocaleId());
        }
        return Lists.newArrayList();
    }

    public List<SelectablePerson> getSearchResults() {
        if (searchResults == null) {
            searchResults = Lists.<SelectablePerson> newArrayList();
        }
        return searchResults;
    }

    public void bindSearchResultTranslator(Long personId,
            boolean asTranslator) {
        SelectablePerson selectablePerson = getPersonFromSearchResult(personId);
        if (selectablePerson != null) {
            selectablePerson.setTranslator(asTranslator);
        }
    }

    public void bindSearchResultReviewer(Long personId, boolean asReviewer) {
        SelectablePerson selectablePerson = getPersonFromSearchResult(personId);
        if (selectablePerson != null) {
            selectablePerson.setReviewer(asReviewer);
        }
    }

    public void bindSearchResultCoordinator(Long personId,
            boolean asCoordinator) {
        SelectablePerson selectablePerson = getPersonFromSearchResult(personId);
        if (selectablePerson != null) {
            selectablePerson.setCoordinator(asCoordinator);
        }
    }

    private SelectablePerson getPersonFromSearchResult(Long personId) {
        for (SelectablePerson selectablePerson : getSearchResults()) {
            if (selectablePerson.getPerson().getId().equals(personId)) {
                return selectablePerson;
            }
        }
        return null;
    }

    public void reset() {
        membersFilter.reset();
    }

    public boolean isUserInTeam() {
        if (authenticatedAccount != null) {
            return languageTeamServiceImpl
                    .getLanguageMemberships(authenticatedAccount.getUsername())
                    .contains(getLocale());
        }
        return false;
    }

    public void addSelected() {
        identity.checkPermission(locale, "manage-language-team");
        for (SelectablePerson selectablePerson : getSearchResults()) {
            if (selectablePerson.isSelected()) {
                addTeamMember(selectablePerson.getPerson().getId(),
                        selectablePerson.isTranslator,
                        selectablePerson.isReviewer,
                        selectablePerson.isCoordinator);
            }
        }
        resetLocale();
    }

    public String getPluralsPlaceholder() {
        String pluralForms = resourceUtils
                .getPluralForms(new LocaleId(language), false, true);
        return msgs.format("jsf.language.plurals.placeholder", pluralForms);
    }

    public String getExamplePluralForms() {
        String pluralForms = resourceUtils
                .getPluralForms(new LocaleId(language), false, true);
        return msgs.format("jsf.language.plurals.example", pluralForms);
    }

    public boolean isValidPluralForms(String pluralForms, String componentId) {
        if (StringUtils.isEmpty(pluralForms)) {
            return true;
        }
        if (resourceUtils.isValidPluralForms(pluralForms)) {
            return true;
        }
        facesMessages.addToControl(componentId,
                msgs.format("jsf.language.plurals.invalid", pluralForms));
        return false;
    }

    public void validatePluralForms(ValueChangeEvent e) {
        isValidPluralForms((String) e.getNewValue(), e.getComponent().getId());
    }

    @CheckRole("admin")
    public void saveSettings() {
        HLocale hLocale = getLocale();
        if (!isValidPluralForms(hLocale.getPluralForms(), "pluralForms")) {
            return;
        }
        hLocale.setDisplayName(hLocale.getDisplayName().trim());
        hLocale.setNativeName(hLocale.getNativeName().trim());
        localeDAO.makePersistent(getLocale());
        facesMessages.addGlobal(
                msgs.format("jsf.language.updated", getLocale().getLocaleId()));
    }

    public HLocale getLocale() {
        /*
         * Preload the HLocaleMember objects. This line is needed as Hibernate
         * has problems when invoking lazily loaded collections from postLoad
         * entity listener methods. In this case, the drools engine will attempt
         * to access the 'members' collection from inside the security
         * listener's postLoad method to evaluate rules.
         */
        if (locale == null && StringUtils.isNotBlank(language)) {
            locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
            locale.getMembers();
        }
        return locale;
    }

    public void validateLanguage() {
        if (StringUtils.isEmpty(language)) {
            redirectToLanguageHome();
            return;
        }
        LocaleId localeId = new LocaleId(language);
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);
        if (locale == null) {
            redirectToLanguageHome();
        } else if (!locale.isActive() && !identity.hasRole("admin")) {
            redirectToLanguageHome();
        }
    }

    private void redirectToLanguageHome() {
        facesMessages.addGlobal(
                msgs.format("jsf.language.validation.NotSupport", language));
        urlUtil.redirectToInternal(urlUtil.languageHome());
    }

    public List<HLocaleMember> getLocaleMembers() {
        return localeMemberDAO.findAllByLocale(new LocaleId(language));
    }

    public String getLocalisedMemberRoles(HLocaleMember member) {
        if (member == null) {
            return "";
        }
        if (member.isCoordinator()) {
            return msgs.get("jsf.Coordinator");
        }
        List<String> roles = Lists.newArrayList();
        if (member.isTranslator()) {
            roles.add(msgs.get("jsf.Translator"));
        }
        if (member.isReviewer()) {
            roles.add(msgs.get("jsf.Reviewer"));
        }
        return Joiner.on(", ").join(roles);
    }

    @Transactional
    @CheckRole("admin")
    public void joinLanguageTeam() {
        if (authenticatedAccount == null) {
            log.error("failed to load auth person");
            return;
        }
        try {
            languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(
                    this.language, authenticatedAccount.getPerson().getId(),
                    true, true, true);
            resetLocale();
            joinLanguageTeamEvent.fire(
                    new JoinedLanguageTeam(authenticatedAccount.getUsername(),
                            new LocaleId(language)));
            log.info("{} joined language team {}",
                    authenticatedAccount.getUsername(), this.language);
            facesMessages.addGlobal(msgs.format("jsf.MemberOfTeam",
                    getLocale().retrieveNativeName()));
        } catch (Exception e) {
            facesMessages.addGlobal(SEVERITY_ERROR, e.getMessage());
        }
    }

    /**
     * Set locale=null to force refresh members list
     */
    private void resetLocale() {
        locale = null;
    }

    @Transactional
    public void leaveLanguageTeam() {
        if (authenticatedAccount == null) {
            log.error("failed to load auth person");
            return;
        }
        languageTeamServiceImpl.leaveLanguageTeam(this.language,
                authenticatedAccount.getPerson().getId());
        resetLocale();
        leaveLanguageTeamEvent.fire(new LeftLanguageTeam(
                authenticatedAccount.getUsername(), new LocaleId(language)));
        log.info("{} left language team {}", authenticatedAccount.getUsername(),
                this.language);
        facesMessages.addGlobal(
                msgs.format("jsf.LeftTeam", getLocale().retrieveNativeName()));
    }

    public void updatePersonRole(Long personId, char localeRoleInitial,
            boolean isPermissionGranted) {
        identity.checkPermission(locale, "manage-language-team");
        LocaleRole role = LocaleRole.valueOf(localeRoleInitial);
        HLocaleMember member = localeMemberDAO.findByPersonAndLocale(personId,
                new LocaleId(language));
        boolean updateAsTranslator = false;
        boolean updateAsReviewer = false;
        boolean updateAsCoordinator = false;
        String permissionDesc = null;
        if (role.equals(LocaleRole.Translator)) {
            updateAsTranslator = isPermissionGranted;
            permissionDesc = msgs.get("jsf.Translator");
        } else if (role.equals(LocaleRole.Reviewer)) {
            updateAsReviewer = isPermissionGranted;
            permissionDesc = msgs.get("jsf.Reviewer");
        } else if (role.equals(LocaleRole.Coordinator)) {
            updateAsCoordinator = isPermissionGranted;
            permissionDesc = msgs.get("jsf.Coordinator");
        }
        if (member == null) {
            HPerson person = personDAO.findById(personId);
            member = new HLocaleMember(person, getLocale(), updateAsTranslator,
                    updateAsReviewer, updateAsCoordinator);
        } else {
            if (role.equals(LocaleRole.Translator)) {
                member.setTranslator(isPermissionGranted);
            } else if (role.equals(LocaleRole.Reviewer)) {
                member.setReviewer(isPermissionGranted);
            } else if (role.equals(LocaleRole.Coordinator)) {
                member.setCoordinator(isPermissionGranted);
            }
        }
        savePermission(member, permissionDesc, role, isPermissionGranted);
    }

    private void savePermission(HLocaleMember member, String permissionDesc,
            LocaleRole role, boolean isPermissionGranted) {
        languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(language,
                member.getPerson().getId(), member.isTranslator(),
                member.isReviewer(), member.isCoordinator());
        resetLocale();
        HPerson person = member.getPerson();
        if (isPermissionGranted) {
            facesMessages.addGlobal(msgs.format("jsf.AddedAPermission",
                    person.getAccount().getUsername(), permissionDesc));
        } else {
            facesMessages.addGlobal(msgs.format("jsf.RemovedAPermission",
                    person.getAccount().getUsername(), permissionDesc));
        }
        HPerson doneByPerson = authenticatedAccount.getPerson();
        LanguageTeamPermissionChangedEvent changedEvent =
                new LanguageTeamPermissionChangedEvent(member.getPerson(),
                        getLocale().getLocaleId(), doneByPerson);
        switch (role) {
        case Translator:
            changedEvent = changedEvent.changedTranslatorPermission(member);
            break;

        case Reviewer:
            changedEvent = changedEvent.changedReviewerPermission(member);
            break;

        case Coordinator:
            changedEvent = changedEvent.changedCoordinatorPermission(member);
            break;

        }
        languageTeamPermissionChangedEvent.fire(changedEvent);
    }

    private void addTeamMember(final Long personId, boolean isTranslator,
            boolean isReviewer, boolean isCoordinator) {
        this.languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(
                this.language, personId, isTranslator, isReviewer,
                isCoordinator);
    }

    public void removeMembership(HLocaleMember member) {
        identity.checkPermission(locale, "manage-language-team");
        this.languageTeamServiceImpl.leaveLanguageTeam(this.language,
                member.getPerson().getId());
        resetLocale();
    }

    public boolean isTranslator(HPerson person) {
        HLocaleMember member = getLocaleMember(person.getId());
        return member == null ? false : member.isTranslator();
    }

    public boolean isReviewer(HPerson person) {
        HLocaleMember member = getLocaleMember(person.getId());
        return member == null ? false : member.isReviewer();
    }

    public boolean isCoordinator(HPerson person) {
        HLocaleMember member = getLocaleMember(person.getId());
        return member == null ? false : member.isCoordinator();
    }

    private HLocaleMember getLocaleMember(final Long personId) {
        for (HLocaleMember lm : getLocaleMembers()) {
            if (lm.getPerson().getId().equals(personId)) {
                return lm;
            }
        }
        return null;
    }

    public boolean hasCoordinators() {
        for (HLocaleMember member : getLocaleMembers()) {
            if (member.isCoordinator()) {
                return true;
            }
        }
        return false;
    }

    public void searchForTeamMembers() {
        clearSearchResult();
        List<HPerson> results =
                this.personDAO.findAllContainingName(this.searchTerm);
        for (HPerson person : results) {
            HLocaleMember localeMember = getLocaleMember(person.getId());
            boolean isMember = localeMember != null;
            boolean isReviewer = false;
            boolean isTranslator = false;
            boolean isCoordinator = false;
            if (isMember) {
                isTranslator = localeMember.isTranslator();
                isReviewer = localeMember.isReviewer();
                isCoordinator = localeMember.isCoordinator();
            }
            getSearchResults().add(new SelectablePerson(person, isMember,
                    isTranslator, isReviewer, isCoordinator));
        }
    }

    public void clearSearchResult() {
        getSearchResults().clear();
    }

    public final class SelectablePerson {
        private HPerson person;
        private boolean selected;
        private boolean isTranslator;
        private boolean isReviewer;
        private boolean isCoordinator;

        public void setReviewer(boolean isReviewer) {
            this.isReviewer = isReviewer;
            refreshSelected();
        }

        public void setCoordinator(boolean isCoordinator) {
            this.isCoordinator = isCoordinator;
            if (isCoordinator) {
                isTranslator = true;
                isReviewer = true;
            }
            refreshSelected();
        }

        public void setTranslator(boolean isTranslator) {
            this.isTranslator = isTranslator;
            refreshSelected();
        }

        private void refreshSelected() {
            this.selected = isReviewer || isTranslator || isCoordinator;
        }

        public HPerson getPerson() {
            return this.person;
        }

        public boolean isSelected() {
            return this.selected;
        }

        public boolean isTranslator() {
            return this.isTranslator;
        }

        public boolean isReviewer() {
            return this.isReviewer;
        }

        public boolean isCoordinator() {
            return this.isCoordinator;
        }

        @java.beans.ConstructorProperties({ "person", "selected",
                "isTranslator", "isReviewer", "isCoordinator" })
        public SelectablePerson(final HPerson person, final boolean selected,
                final boolean isTranslator, final boolean isReviewer,
                final boolean isCoordinator) {
            this.person = person;
            this.selected = selected;
            this.isTranslator = isTranslator;
            this.isReviewer = isReviewer;
            this.isCoordinator = isCoordinator;
        }
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getSearchTerm() {
        return this.searchTerm;
    }

    public void setSearchTerm(final String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public AbstractListFilter<HLocaleMember> getMembersFilter() {
        return this.membersFilter;
    }
}
