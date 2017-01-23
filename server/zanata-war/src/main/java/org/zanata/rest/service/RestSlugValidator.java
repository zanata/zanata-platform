/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.WebApplicationException;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.service.LocaleService;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Dependent
@Named("restSlugValidator")
public class RestSlugValidator {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RestSlugValidator.class);

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Nonnull
    public HProject retrieveAndCheckProject(@Nonnull String projectSlug,
            boolean requiresWriteAccess) {
        HProject hProject = projectDAO.getBySlug(projectSlug);
        if (hProject == null
                || hProject.getStatus().equals(EntityStatus.OBSOLETE)) {
            throw new NoSuchEntityException(
                    "Project \'" + projectSlug + "\' not found.");
        }
        if (requiresWriteAccess
                && hProject.getStatus().equals(EntityStatus.READONLY)) {
            throw new ReadOnlyEntityException(
                    "Project \'" + projectSlug + "\' is read-only.");
        }
        return hProject;
    }

    /**
     * @param requiresWriteAccess
     * @return
     * @see ProjectService#retrieveAndCheckProject
     */
    @Nonnull
    public HProjectIteration retrieveAndCheckIteration(
            @Nonnull String projectSlug, @Nonnull String iterationSlug,
            boolean requiresWriteAccess) {
        HProject hProject =
                retrieveAndCheckProject(projectSlug, requiresWriteAccess);
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(hProject, iterationSlug);
        if (hProjectIteration == null || hProjectIteration.getStatus()
                .equals(EntityStatus.OBSOLETE)) {
            throw new NoSuchEntityException("Project Iteration \'" + projectSlug
                    + ":" + iterationSlug + "\' not found.");
        }
        if (requiresWriteAccess && hProjectIteration.getStatus()
                .equals(EntityStatus.READONLY)) {
            throw new ReadOnlyEntityException("Project Iteration \'"
                    + projectSlug + ":" + iterationSlug + "\' is read-only.");
        }
        return hProjectIteration;
    }

    /**
     * Returns the requested locale, but only if the locale is allowed for the
     * server/project.
     *
     * @param locale
     * @param projectSlug
     * @return
     * @throws WebApplicationException
     *             if locale is not allowed
     */
    @Nonnull
    public HLocale validateTargetLocale(@Nonnull LocaleId locale,
            @Nonnull String projectSlug) {
        try {
            return localeServiceImpl.validateLocaleByProject(locale,
                    projectSlug);
        } catch (ZanataServiceException e) {
            log.warn("Exception validating target locale {} in proj {}", e,
                    locale, projectSlug);
            throw e;
        }
    }

    /**
     * Returns the requested locale, but only if the locale is allowed for the
     * server/project/version.
     *
     * @param locale
     * @param projectSlug
     * @param iterationSlug
     * @return
     * @throws WebApplicationException
     *             if locale is not allowed
     */
    @Nonnull
    public HLocale validateTargetLocale(@Nonnull LocaleId locale,
            @Nonnull String projectSlug, @Nonnull String iterationSlug) {
        try {
            return localeServiceImpl.validateLocaleByProjectIteration(locale,
                    projectSlug, iterationSlug);
        } catch (ZanataServiceException e) {
            log.warn("Exception validating target locale {} in proj {} iter {}",
                    e, locale, projectSlug, iterationSlug);
            throw e;
        }
    }
}
