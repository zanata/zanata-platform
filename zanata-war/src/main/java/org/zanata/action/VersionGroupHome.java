/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.FlashScopeBean;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;
import org.zanata.service.VersionGroupService;
import org.zanata.util.ZanataMessages;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHome")
public class VersionGroupHome extends SlugHome<HIterationGroup> {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String slug;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private SlugEntityService slugEntityServiceImpl;

    @In
    private LocaleService localeServiceImpl;

    @In
    private PersonDAO personDAO;

    @In
    private VersionGroupService versionGroupServiceImpl;

    @In
    private ZanataMessages zanataMessages;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private FlashScopeBean flashScope;

    private List<HLocale> supportedLocales;

    private List<SelectItem> statusList;

    @Getter
    @Setter
    // locale id
    private String newLanguage;

    @Getter
    @Setter
    // Slug of project [space] version
    private String newVersion;

    @Getter
    @Setter
    // username
    private String newMaintainer;

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            FacesMessages.instance().addToControl(componentId,
                    "This Group ID is not available");
            return false;
        }
        return true;
    }

    public boolean isSlugAvailable(String slug) {
        return slugEntityServiceImpl.isSlugAvailable(slug,
                HIterationGroup.class);
    }

    @Override
    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public String persist() {
        if (!validateSlug(getInstance().getSlug(), "slug"))
            return null;

        if (authenticatedAccount != null) {
            getInstance().addMaintainer(authenticatedAccount.getPerson());
        }
        clearMessage();
        return super.persist();
    }

    @Override
    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public String update() {
        clearMessage();
        return super.update();
    }

    @Override
    public List<SelectItem> getStatusList() {
        return getAvailableStatus();
    }

    public void setStatus(char initial) {
        getInstance().setStatus(EntityStatus.valueOf(initial));
    }

    public List<HLocale> suggestLocales(final String query) {
        if (supportedLocales == null) {
            supportedLocales = localeServiceImpl.getSupportedLocales();
        }

        Collection<HLocale> filtered =
                Collections2.filter(supportedLocales, new Predicate<HLocale>() {
                    @Override
                    public boolean apply(@Nullable HLocale input) {
                        return input.getLocaleId().getId().startsWith(query)
                                || input.retrieveDisplayName().toLowerCase()
                                        .contains(query.toLowerCase());
                    }
                });

        return Lists.newArrayList(filtered);
    }

    public List<HProjectIteration> suggestVersions(final String query) {
        return versionGroupServiceImpl.searchLikeSlugOrProjectSlug(query);
    }

    public List<HPerson> suggestMaintainers(final String query) {
        return personDAO.findAllContainingName(query);
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void addLanguage() {
        if (!validateLanguageName("newLanguage")) {
            return; // not success
        }
        HLocale locale =
                localeServiceImpl.getByLocaleId(new LocaleId(newLanguage));
        getInstance().getActiveLocales().add(locale);
        super.update();
        this.newLanguage = "";

        addMessage(
                StatusMessage.Severity.INFO,
                zanataMessages.getMessage("jsf.LanguageAddedToGroup",
                        locale.retrieveDisplayName()));
    }

    /**
     * Use FlashScopeBean to store message in page. Multiple ajax requests for
     * re-rendering statistics after updating will clear FacesMessages.
     *
     * @param severity
     * @param message
     */
    private void addMessage(StatusMessage.Severity severity, String message) {
        StatusMessage statusMessage =
                new StatusMessage(severity, null, null, message, null);
        statusMessage.interpolate();

        flashScope.setAttribute("message", statusMessage);
    }

    private void clearMessage() {
        flashScope.getAndClearAttribute("message");
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void addVersion() {
        if (!validateVersion("newVersion")) {
            return; // not success
        }
        String[] slugs = newVersion.split(" ");

        HProjectIteration version =
                projectIterationDAO.getBySlug(slugs[0], slugs[1]);
        getInstance().getProjectIterations().add(version);
        super.update();
        this.newVersion = "";

        addMessage(
                StatusMessage.Severity.INFO,
                zanataMessages.getMessage("jsf.VersionAddedToGroup",
                        version.getSlug()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void addMaintainer() {
        if (!validateMaintainer("newMaintainer")) {
            return; // not success
        }
        HPerson maintainer = personDAO.findByUsername(newMaintainer);
        getInstance().getMaintainers().add(maintainer);
        super.update();
        this.newMaintainer = "";

        addMessage(StatusMessage.Severity.INFO, zanataMessages.getMessage(
                "jsf.MaintainerAddedToGroup", maintainer.getName()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void removeLanguage(HLocale locale) {
        getInstance().getActiveLocales().remove(locale);
        super.update();
        addMessage(
                StatusMessage.Severity.INFO,
                zanataMessages.getMessage("jsf.LanguageRemoveFromGroup",
                        locale.retrieveDisplayName()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void removeVersion(HProjectIteration version) {
        getInstance().getProjectIterations().remove(version);
        super.update();

        addMessage(
                StatusMessage.Severity.INFO,
                zanataMessages.getMessage("jsf.VersionRemoveFromGroup",
                        version.getSlug()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void removeMaintainer(HPerson maintainer) {
        getInstance().getMaintainers().remove(maintainer);
        super.update();

        addMessage(StatusMessage.Severity.INFO, zanataMessages.getMessage(
                "jsf.MaintainerRemoveFromGroup", maintainer.getName()));
    }

    public List<HLocale> getInstanceActiveLocales() {
        List<HLocale> activeLocales =
                Lists.newArrayList(getInstance().getActiveLocales());

        Collections.sort(activeLocales, new Comparator<HLocale>() {
            @Override
            public int compare(HLocale hLocale, HLocale hLocale2) {
                return hLocale.retrieveDisplayName().compareTo(
                        hLocale2.retrieveDisplayName());
            }
        });
        return activeLocales;
    }

    private List<SelectItem> getAvailableStatus() {
        if (statusList == null) {
            statusList =
                    ImmutableList.copyOf(Iterables.filter(
                            super.getStatusList(), new Predicate<SelectItem>() {
                                @Override
                                public boolean apply(SelectItem input) {
                                    return !input.getValue().equals(
                                            EntityStatus.READONLY);
                                }
                            }));
        }
        return statusList;
    }

    @Override
    protected HIterationGroup loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HIterationGroup) session.byNaturalId(HIterationGroup.class)
                .using("slug", getSlug()).load();
    }

    public List<HProjectIteration> getInstanceProjectIterations() {
        return new ArrayList<HProjectIteration>(getInstance()
                .getProjectIterations());
    }

    public List<HPerson> getInstanceMaintainers() {
        return new ArrayList<HPerson>(getInstance().getMaintainers());
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", slug);
    }

    @Override
    public boolean isIdDefined() {
        return slug != null;
    }

    @Override
    public Object getId() {
        return slug;
    }

    public void validateSuppliedId() {
        getInstance(); // this will raise an EntityNotFound exception
        // when id is invalid and conversation will not
        // start
    }

    public boolean validateLanguageName(String componentId) {
        // Check that locale Id is syntactically valid
        LocaleId localeId;
        if (StringUtils.isEmpty(newLanguage)) {
            return false;
        }

        try {
            localeId = new LocaleId(newLanguage);
        } catch (IllegalArgumentException iaex) {
            FacesMessages.instance().addToControl(
                    componentId,
                    zanataMessages
                            .getMessage("jsf.language.validation.Invalid"));
            return false;
        }

        // check if locale enabled in server
        if (!localeServiceImpl.localeSupported(localeId)) {
            FacesMessages.instance().addToControl(
                    componentId,
                    zanataMessages
                            .getMessage("jsf.language.validation.NotSupport"),
                    localeId);
            return false;
        }

        // check for already added language
        for (HLocale locale : getInstanceActiveLocales()) {
            if (locale.getLocaleId().equals(localeId)) {
                FacesMessages
                        .instance()
                        .addToControl(
                                componentId,
                                zanataMessages
                                        .getMessage("jsf.LanguageAlreadyInGroup"),
                                localeId);
                return false;
            }
        }

        return true;
    }

    public boolean validateVersion(String componentId) {
        newVersion = newVersion.trim();
        if (StringUtils.isEmpty(newVersion)
                || !StringUtils.contains(newVersion, " ")) {
            return false;
        }
        String[] slugs = newVersion.split(" ");

        HProjectIteration version =
                projectIterationDAO.getBySlug(slugs[0], slugs[1]);
        // check if version exists
        if (version == null) {
            FacesMessages.instance().addToControl(componentId,
                    zanataMessages.getMessage("jsf.InvalidProjectVersion"));
            return false;
        }

        // check if version is already in group
        if (getInstanceProjectIterations().contains(version)) {
            FacesMessages.instance().addToControl(
                    componentId,
                    zanataMessages.getMessage("jsf.VersionAlreadyInGroup",
                            version.getSlug()));
            return false;
        }

        return true;
    }

    public boolean validateMaintainer(String componentId) {
        if (StringUtils.isEmpty(newMaintainer)) {
            return false;
        }

        // check if username exist
        HPerson person = personDAO.findByUsername(newMaintainer);
        if (person == null) {
            FacesMessages.instance().addToControl(componentId,
                    zanataMessages.getMessage("jsf.InvalidUsername"));
            return false;
        }

        // check if person is already a maintainer
        if (getInstanceMaintainers().contains(person)) {
            FacesMessages.instance().addToControl(
                    componentId,
                    zanataMessages.getMessage("jsf.UserIsAMaintainer",
                            newMaintainer));
            return false;
        }
        return true;
    }

    public String cancel() {
        return "cancel";
    }
}
