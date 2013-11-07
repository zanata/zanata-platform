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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationAction;

@Name("projectHome")
public class ProjectHome extends SlugHome<HProject> {
    private static final long serialVersionUID = 1L;

    public static final String PROJECT_UPDATE = "project.update";

    @Getter
    @Setter
    private String slug;

    @In
    ZanataIdentity identity;

    @Logger
    Log log;

    @In
    private PersonDAO personDAO;

    @In
    private LocaleDAO localeDAO;

    @In
    private ValidationService validationServiceImpl;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;

    /* Outjected from LocaleListAction */
    @In(required = false)
    Map<String, String> customizedItems;

    /* Outjected from LocaleListAction */
    @In(required = false)
    private Boolean overrideLocales;

    /* Outjected from ProjectRoleRestrictionAction */
    @In(required = false)
    private Set<HAccountRole> customizedProjectRoleRestrictions;

    /* Outjected from ProjectRoleRestrictionAction */
    @In(required = false)
    private Boolean restrictByRoles;

    /* Outjected from ValidationOptionsAction */
    @In(required = false)
    private Collection<ValidationAction> customizedValidations;

    @In
    private LocaleService localeServiceImpl;

    @In
    private SlugEntityService slugEntityServiceImpl;

    @In(create = true)
    private ProjectDAO projectDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private EntityManager entityManager;

    @Override
    protected HProject loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HProject) session.byNaturalId(HProject.class)
                .using("slug", getSlug()).load();
    }

    public void validateSuppliedId() {
        HProject ip = getInstance(); // this will raise an EntityNotFound
                                     // exception
        // when id is invalid and conversation will not
        // start

        if (ip.getStatus().equals(EntityStatus.OBSOLETE)
                && !checkViewObsolete()) {
            throw new EntityNotFoundException();
        }
    }

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            FacesMessages.instance().addToControl(componentId,
                    "This Project ID is not available");
            return false;
        }
        return true;
    }

    public boolean isSlugAvailable(String slug) {
        return slugEntityServiceImpl.isSlugAvailable(slug, HProject.class);
    }

    @Override
    @Transactional
    public String persist() {
        String retValue = "";
        if (!validateSlug(getInstance().getSlug(), "slug"))
            return null;

        if (authenticatedAccount != null) {
            updateOverrideLocales();
            updateRoleRestrictions();
            updateOverrideValidations();
            getInstance().addMaintainer(authenticatedAccount.getPerson());
            retValue = super.persist();
            Events.instance().raiseEvent("projectAdded");
        }
        return retValue;
    }

    public List<HProjectIteration> getVersions() {
        List<HProjectIteration> results = new ArrayList<HProjectIteration>();

        for (HProjectIteration iteration : getInstance().getProjectIterations()) {
            if (iteration.getStatus() == EntityStatus.OBSOLETE
                    && checkViewObsolete()) {
                results.add(iteration);
            } else if (iteration.getStatus() != EntityStatus.OBSOLETE) {
                results.add(iteration);
            }
        }
        Collections.sort(results, new Comparator<HProjectIteration>() {
            @Override
            public int compare(HProjectIteration o1, HProjectIteration o2) {
                EntityStatus fromStatus = o1.getStatus();
                EntityStatus toStatus = o2.getStatus();

                if (fromStatus.equals(toStatus)) {
                    return 0;
                }

                if (fromStatus.equals(EntityStatus.ACTIVE)) {
                    return -1;
                }

                if (fromStatus.equals(EntityStatus.READONLY)) {
                    if (toStatus.equals(EntityStatus.ACTIVE)) {
                        return 1;
                    }
                    return -1;
                }

                if (fromStatus.equals(EntityStatus.OBSOLETE)) {
                    return 1;
                }

                return 0;
            }
        });
        return results;
    }

    public EntityStatus getEffectiveVersionStatus(HProjectIteration version) {
        /**
         * Null pointer exception checking caused by unknown issues where
         * getEffectiveIterationStatus gets invoke before getIterations
         */
        if (version == null) {
            return null;
        }
        if (getInstance().getStatus() == EntityStatus.READONLY) {
            if (version.getStatus() == EntityStatus.ACTIVE) {
                return EntityStatus.READONLY;
            }
        } else if (getInstance().getStatus() == EntityStatus.OBSOLETE) {
            if (version.getStatus() == EntityStatus.ACTIVE
                    || version.getStatus() == EntityStatus.READONLY) {
                return EntityStatus.OBSOLETE;
            }
        }
        return version.getStatus();
    }

    public String cancel() {
        return "cancel";
    }

    @Override
    public boolean isIdDefined() {
        return slug != null;
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", slug);
    }

    @Override
    public Object getId() {
        return slug;
    }

    @Override
    public String update() {
        updateOverrideLocales();
        updateRoleRestrictions();
        updateOverrideValidations();
        String state = super.update();
        Events.instance().raiseEvent(PROJECT_UPDATE, getInstance());

        if (getInstance().getStatus() == EntityStatus.READONLY) {
            for (HProjectIteration version : getInstance()
                    .getProjectIterations()) {
                if (version.getStatus() == EntityStatus.ACTIVE) {
                    version.setStatus(EntityStatus.READONLY);
                    entityManager.merge(version);
                    Events.instance().raiseEvent(
                            ProjectIterationHome.PROJECT_ITERATION_UPDATE,
                            version);
                }
            }
        } else if (getInstance().getStatus() == EntityStatus.OBSOLETE) {
            for (HProjectIteration version : getInstance()
                    .getProjectIterations()) {
                if (version.getStatus() != EntityStatus.OBSOLETE) {
                    version.setStatus(EntityStatus.OBSOLETE);
                    entityManager.merge(version);
                    Events.instance().raiseEvent(
                            ProjectIterationHome.PROJECT_ITERATION_UPDATE,
                            version);
                }
            }
        }

        return state;
    }

    private void updateOverrideLocales() {
        if (overrideLocales != null) {
            getInstance().setOverrideLocales(overrideLocales);
            if (!overrideLocales) {
                getInstance().getCustomizedLocales().clear();
            } else if (customizedItems != null) {
                Set<HLocale> locale =
                        localeServiceImpl
                                .convertCustomizedLocale(customizedItems);
                getInstance().getCustomizedLocales().clear();
                getInstance().getCustomizedLocales().addAll(locale);
            }
        }
    }

    private void updateOverrideValidations() {
        getInstance().getCustomizedValidations().clear();
        for (ValidationAction action : customizedValidations) {
            getInstance().getCustomizedValidations().put(action.getId().name(),
                    action.getState().name());
        }
    }

    private void updateRoleRestrictions() {
        if (restrictByRoles != null) {
            getInstance().setRestrictedByRoles(restrictByRoles);
            getInstance().getAllowedRoles().clear();

            if (restrictByRoles) {
                getInstance().getAllowedRoles().addAll(
                        customizedProjectRoleRestrictions);
            }
        }
    }

    public boolean isProjectActive() {
        return getInstance().getStatus() == EntityStatus.ACTIVE;
    }

    public boolean checkViewObsolete() {
        return identity != null
                && identity.hasPermission("HProject", "view-obsolete");
    }

    public boolean isUserAllowedToTranslateOrReview(String versionSlug,
            HLocale localeId) {
        return !StringUtils.isEmpty(versionSlug)
                && localeId != null
                && isIterationActive(versionSlug)
                && identity != null
                && (identity.hasPermission("add-translation", getInstance(),
                        localeId) || identity.hasPermission(
                        "translation-review", getInstance(), localeId));
    }

    private boolean isIterationActive(String versionSlug) {
        HProjectIteration version =
                projectIterationDAO.getBySlug(getSlug(), versionSlug);
        return getInstance().getStatus() == EntityStatus.ACTIVE
                || version.getStatus() == EntityStatus.ACTIVE;
    }
}
