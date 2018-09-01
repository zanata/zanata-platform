/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HasEntityStatus;
import org.zanata.service.LocaleService;
import com.google.common.base.Objects;

import static org.zanata.common.EntityStatus.OBSOLETE;
import static org.zanata.common.EntityStatus.READONLY;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class ActiveProjectVersionAndLocaleValidator {
    private ProjectDAO projectDAO;
    private LocaleService localeService;
    private ProjectIterationDAO projectIterationDAO;
    private HProject project;
    private HLocale hLocale;
    private HProjectIteration version;


    @Inject
    public ActiveProjectVersionAndLocaleValidator(ProjectDAO projectDAO,
            LocaleService localeService,
            ProjectIterationDAO projectIterationDAO) {
        this.projectDAO = projectDAO;
        this.localeService = localeService;
        this.projectIterationDAO = projectIterationDAO;
    }

    public ActiveProjectVersionAndLocaleValidator() {
    }

    public static Optional<Response> getResponseIfEntityIsNotActive(
            HasEntityStatus entity, String slug, String entityName) {
        if (entity == null) {
            return Optional.of(Response.status(Response.Status.NOT_FOUND)
                    .entity(entityName + " \'" + slug + "\' not found.")
                    .build());
        } else if (Objects.equal(entity.getStatus(), OBSOLETE)) {
            // is Obsolete
            return Optional.of(Response.status(Response.Status.NOT_FOUND)
                    .entity(entityName + " \'" + slug + "\' not found.")
                    .build());
        } else if (Objects.equal(entity.getStatus(), READONLY)) {
            // is ReadOnly
            return Optional.of(Response.status(Response.Status.FORBIDDEN)
                    .entity(entityName + " \'" + slug + "\' is read-only.")
                    .build());
        }
        return Optional.empty();
    }

    public Optional<Response> getResponseIfProjectIsNotActive(String projectSlug) {
        project = projectDAO.getBySlug(projectSlug);
        return getResponseIfEntityIsNotActive(project, projectSlug, "Project");
    }

    public Optional<Response> getResponseIfProjectVersionIsNotActive(String versionSlug) {
        version = projectIterationDAO.getBySlug(project, versionSlug);
        return getResponseIfEntityIsNotActive(version, versionSlug, "Project version");
    }


    public Optional<Response> getResponseIfProjectLocaleAndVersionAreNotActive(
            String projectSlug,
            String versionSlug, LocaleId localeId) {

        Optional<Response> projectResponse = getResponseIfProjectIsNotActive(projectSlug);
        if (projectResponse.isPresent()) {
            return projectResponse;
        }

        hLocale = localeService.getByLocaleId(localeId);
        if (hLocale == null) {
            return Optional
                    .of(Response.status(Response.Status.NOT_FOUND).build());
        }

        return getResponseIfProjectVersionIsNotActive(versionSlug);
    }

    public HProject getProject() {
        return project;
    }

    public HLocale getLocale() {
        return hLocale;
    }

    public HProjectIteration getVersion() {
        return version;
    }
}
