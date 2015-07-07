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
import java.util.ArrayList;
import java.util.List;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.security.management.JpaIdentityStore;
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
import org.zanata.rest.editor.dto.Locale;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.Event;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.util.ServiceLocator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Name("languageAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class LanguageAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private LanguageTeamService languageTeamServiceImpl;

    @In
    private LocaleDAO localeDAO;

    @In
    private PersonDAO personDAO;

    @In
    private LocaleService localeServiceImpl;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private ZanataIdentity identity;

    @In
    private Messages msgs;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In("event")
    private Event<JoinedLanguageTeam> joinLanguageTeamEvent;

    @In("event")
    private Event<LanguageTeamPermissionChangedEvent> languageTeamPermissionChangedEvent;

    @In("event")
    private Event<LeftLanguageTeam> leaveLanguageTeamEvent;

    @In
    private LocaleMemberDAO localeMemberDAO;

    @In
    private ResourceUtils resourceUtils;

    @In
    private Redirect redirect;

    @Getter
    @Setter
    private String language;

    @Getter
    @Setter
    private String searchTerm;

    private HLocale locale;

    private List<SelectablePerson> searchResults;

    @Getter
    private AbstractListFilter<HLocaleMember> membersFilter =
            new InMemoryListFilter<HLocaleMember>() {
                @Override
                protected List<HLocaleMember> fetchAll() {
                    ServiceLocator serviceLocator = ServiceLocator.instance();
                    LocaleMemberDAO localeMemberDAO =
                            serviceLocator.getInstance(LocaleMemberDAO.class);

                    return localeMemberDAO.findAllByLocale(
                            new LocaleId(language));
                }

                @Override
                protected boolean include(HLocaleMember elem,
                        String filter) {
                    return StringUtils.containsIgnoreCase(
                            elem.getPerson().getName(), filter);
                }
            };

    public List<SelectablePerson> getSearchResults() {
        if (searchResults == null) {
            searchResults = new ArrayList<>();
        }

        return searchResults;
    }

    public void reset() {
        membersFilter.reset();
    }

    public boolean isUserInTeam() {
        if(authenticatedAccount != null) {
            return languageTeamServiceImpl
                .getLanguageMemberships(authenticatedAccount.getUsername()).contains(getLocale());
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
        String pluralForms = resourceUtils.getPluralForms(new LocaleId(language), false, true);
        return msgs.format("jsf.language.plurals.placeholder", pluralForms);
    }

    public String getExamplePluralForms() {
        String pluralForms = resourceUtils.getPluralForms(new LocaleId(language), false, true);
        return msgs.format("jsf.language.plurals.example", pluralForms);
    }

    public boolean isValidPluralForms(String pluralForms, String componentId) {
        if(StringUtils.isEmpty(pluralForms)) {
            return true;
        }
        if(resourceUtils.isValidPluralForms(pluralForms)) {
            return true;
        }
        facesMessages.addToControl(componentId,
                msgs.format("jsf.language.plurals.invalid", pluralForms));
        return false;
    }

    public void validatePluralForms(ValueChangeEvent e) {
        isValidPluralForms((String) e.getNewValue(), e.getComponent().getId());
    }

    @Restrict("#{s:hasRole('admin')}")
    public void saveSettings() {
        HLocale hLocale = getLocale();
        if(!isValidPluralForms(hLocale.getPluralForms(), "pluralForms")) {
            return;
        }
        hLocale.setDisplayName(hLocale.getDisplayName().trim());
        hLocale.setNativeName(hLocale.getNativeName().trim());
        localeDAO.makePersistent(getLocale());
        facesMessages.addGlobal(msgs.format("jsf.language.updated", getLocale()
                .getLocaleId()));
    }

    public HLocale getLocale() {
        /*
         * Preload the HLocaleMember objects. This line is needed as Hibernate
         * has problems when invoking lazily loaded collections from postLoad
         * entity listener methods. In this case, the drools engine will attempt
         * to access the 'members' collection from inside the security
         * listener's postLoad method to evaluate rules.
         */
        if(locale == null) {
            locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
            locale.getMembers();
        }
        return locale;
    }

    public void validateLanguage() {
        if(StringUtils.isEmpty(language)) {
            redirectToLanguageHome();
            return;
        }

        LocaleId localeId = new LocaleId(language);
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);

        if(locale == null) {
            redirectToLanguageHome();
        }

        if(!locale.isActive() && !identity.hasRole("admin")) {
            redirectToLanguageHome();
        }
    }

    private void redirectToLanguageHome() {
        facesMessages.addGlobal(msgs.format(
            "jsf.language.validation.NotSupport", language));
        redirect.setViewId("/language/home.xhtml");
        redirect.execute();
    }


    public List<HLocaleMember> getLocaleMembers() {
        return localeMemberDAO.findAllByLocale(new LocaleId(language));
    }

    @Transactional
    @Restrict("#{s:hasRole('admin')}")
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
            joinLanguageTeamEvent.fire(new JoinedLanguageTeam(authenticatedAccount.getUsername(), new LocaleId(language)));
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
        leaveLanguageTeamEvent.fire(new LeftLanguageTeam(authenticatedAccount.getUsername(), new LocaleId(language)));
        log.info("{} left language team {}", authenticatedAccount.getUsername(),
                this.language);
        facesMessages.addGlobal(msgs.format("jsf.LeftTeam",
                getLocale().retrieveNativeName()));
    }

    public void saveTeamCoordinator(HLocaleMember member) {
        identity.checkPermission(locale, "manage-language-team");
        savePermission(member, msgs.get("jsf.Coordinator"), member.isCoordinator());
        HPerson doneByPerson = authenticatedAccount.getPerson();
        LanguageTeamPermissionChangedEvent changedEvent =
                new LanguageTeamPermissionChangedEvent(
                        member.getPerson(), getLocale().getLocaleId(),
                        doneByPerson)
                         .changedCoordinatorPermission(member);
        languageTeamPermissionChangedEvent.fire(changedEvent);
    }

    public void saveTeamReviewer(HLocaleMember member) {
        identity.checkPermission(locale, "manage-language-team");
        savePermission(member, msgs.get("jsf.Reviewer"), member.isReviewer());
        HPerson doneByPerson = authenticatedAccount.getPerson();
        LanguageTeamPermissionChangedEvent changedEvent =
                new LanguageTeamPermissionChangedEvent(
                        member.getPerson(), getLocale().getLocaleId(),
                        doneByPerson)
                        .changedReviewerPermission(member);
        languageTeamPermissionChangedEvent.fire(changedEvent);
    }

    public void saveTeamTranslator(HLocaleMember member) {
        identity.checkPermission(locale, "manage-language-team");
        savePermission(member, msgs.get("jsf.Translator"), member.isTranslator());
        HPerson doneByPerson = authenticatedAccount.getPerson();
        LanguageTeamPermissionChangedEvent changedEvent =
                new LanguageTeamPermissionChangedEvent(
                        member.getPerson(), getLocale().getLocaleId(),
                        doneByPerson)
                        .changedTranslatorPermission(member);
        languageTeamPermissionChangedEvent.fire(changedEvent);
    }

    private void savePermission(HLocaleMember member, String permissionDesc,
            boolean isPermissionGranted) {
        languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(
            this.language, member.getPerson().getId(),
            member.isTranslator(), member.isReviewer(), member.isCoordinator());
        resetLocale();
        HPerson person = member.getPerson();
        if (isPermissionGranted) {
            facesMessages.addGlobal(
                    msgs.format("jsf.AddedAPermission",
                    person.getAccount().getUsername(), permissionDesc));
        } else {
            facesMessages.addGlobal(
                    msgs.format("jsf.RemovedAPermission",
                            person.getAccount().getUsername(), permissionDesc));
        }
    }

    private void addTeamMember(final Long personId, boolean isTranslator,
        boolean isReviewer, boolean isCoordinator) {
        this.languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(
                this.language, personId, isTranslator, isReviewer,
                isCoordinator);
    }

    public void removeMembership(HLocaleMember member) {
        identity.checkPermission(locale, "manage-language-team");
        this.languageTeamServiceImpl.leaveLanguageTeam(this.language, member
                .getPerson().getId());
        resetLocale();
    }

    private HLocaleMember getLocaleMember(final Long personId) {
        for (HLocaleMember lm : getLocaleMembers()) {
            if (lm.getPerson().getId().equals(personId)) {
                return lm;
            }
        }
        return null;
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
            getSearchResults().add(
                    new SelectablePerson(person, isMember, isTranslator,
                            isReviewer, isCoordinator));
        }
    }

    public void clearSearchResult() {
        getSearchResults().clear();
    }

    public final class SelectablePerson {
        @Getter
        private HPerson person;

        @Getter
        private boolean selected;

        @Getter
        private boolean isReviewer;

        @Getter
        private boolean isCoordinator;

        @Getter
        private boolean isTranslator;

        public SelectablePerson(HPerson person, boolean selected,
                boolean isTranslator, boolean isReviewer, boolean isCoordinator) {
            this.person = person;
            this.selected = selected;
            this.isReviewer = isReviewer;
            this.isCoordinator = isCoordinator;
            this.isTranslator = isTranslator;
        }

        public void setReviewer(boolean isReviewer) {
            this.isReviewer = isReviewer;
            refreshSelected();
        }

        public void setCoordinator(boolean isCoordinator) {
            this.isCoordinator = isCoordinator;
            refreshSelected();
        }

        public void setTranslator(boolean isTranslator) {
            this.isTranslator = isTranslator;
            refreshSelected();
        }

        private void refreshSelected() {
            this.selected = isReviewer || isTranslator || isCoordinator;
        }
    }

}
