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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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

import javax.annotation.Nullable;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private List<SelectItem> statusList;

    @Setter
    private String languageQuery;

    @Setter
    // localeId
    private String selectedLocale;

    @Setter
    private String versionQuery;

    @Setter
    // version id
    private String selectedVersion;

    @Setter
    private String maintainerQuery;

    @Setter
    // username
    private String selectedMaintainer;

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

    public List<HLocale> suggestLocales() {
        if (StringUtils.isEmpty(languageQuery)) {
            return Lists.newArrayList();
        }

        List<HLocale> localeList = localeServiceImpl.getSupportedLocales();

        Collection<HLocale> filtered =
                Collections2.filter(localeList, new Predicate<HLocale>() {
                    @Override
                    public boolean apply(@Nullable HLocale input) {
                        return !getInstance().getActiveLocales()
                                .contains(input)
                                && (input.getLocaleId().getId()
                                        .startsWith(languageQuery) || input
                                        .retrieveDisplayName().toLowerCase()
                                        .contains(languageQuery.toLowerCase()));
                    }
                });

        return Lists.newArrayList(filtered);
    }

    public List<HProjectIteration> suggestVersions() {
        List<HProjectIteration> versionList =
                versionGroupServiceImpl
                        .searchLikeSlugOrProjectSlug(versionQuery);

        Collection<HProjectIteration> filtered =
                Collections2.filter(versionList,
                        new Predicate<HProjectIteration>() {
                            @Override
                            public boolean apply(
                                    @Nullable HProjectIteration input) {
                                return !input.getGroups().contains(
                                        getInstance());
                            }
                        });

        return Lists.newArrayList(filtered);
    }

    public List<HPerson> suggestMaintainers() {
        List<HPerson> personList =
                personDAO.findAllContainingName(maintainerQuery);

        Collection<HPerson> filtered =
                Collections2.filter(personList, new Predicate<HPerson>() {
                    @Override
                    public boolean apply(@Nullable HPerson input) {
                        return !getInstance().getMaintainers().contains(input);
                    }
                });

        return Lists.newArrayList(filtered);
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
    public void addLanguage() {
        if (StringUtils.isEmpty(selectedLocale)) {
            return;
        }

        HLocale locale = localeServiceImpl.getByLocaleId(selectedLocale);

        getInstance().getActiveLocales().add(locale);
        super.update();
        selectedLocale = "";
        languageQuery = "";

        addMessage(
                StatusMessage.Severity.INFO,
                zanataMessages.getMessage("jsf.LanguageAddedToGroup",
                        locale.retrieveDisplayName()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void addVersion() {
        if (StringUtils.isEmpty(selectedVersion)) {
            return;
        }

        HProjectIteration version =
                projectIterationDAO.findById(new Long(selectedVersion));
        getInstance().getProjectIterations().add(version);
        super.update();
        selectedVersion = "";
        versionQuery = "";

        addMessage(
                StatusMessage.Severity.INFO,
                zanataMessages.getMessage("jsf.VersionAddedToGroup",
                        version.getSlug(), version.getProject().getSlug()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void addMaintainer() {
        if (StringUtils.isEmpty(selectedMaintainer)) {
            return;
        }

        HPerson maintainer = personDAO.findByUsername(selectedMaintainer);
        getInstance().getMaintainers().add(maintainer);
        super.update();
        selectedMaintainer = "";
        maintainerQuery = "";

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
                        version.getSlug(), version.getProject().getSlug()));
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

    // sort by slug
    public List<HProjectIteration> getSortedInstanceProjectIterations() {
        List<HProjectIteration> list =
                Lists.newArrayList(getInstance().getProjectIterations());

        Collections.sort(list, new Comparator<HProjectIteration>() {
            @Override
            public int compare(HProjectIteration documentWithIds,
                    HProjectIteration documentWithIds2) {
                return documentWithIds
                        .getProject()
                        .getName()
                        .toLowerCase()
                        .compareTo(
                                documentWithIds2.getProject().getName()
                                        .toLowerCase());
            }
        });

        return list;
    }

    public List<HPerson> getInstanceMaintainers() {
        List<HPerson> list = Lists.newArrayList(getInstance().getMaintainers());

        Collections.sort(list, PERSON_COMPARATOR);

        return list;
    }

    public final static Comparator<HPerson> PERSON_COMPARATOR =
            new Comparator<HPerson>() {
                @Override
                public int compare(HPerson hPerson, HPerson hPerson2) {
                    return hPerson.getName().compareTo(hPerson2.getName());
                }
            };

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

    public String cancel() {
        return "cancel";
    }
}
