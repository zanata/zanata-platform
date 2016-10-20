/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps between document names used by legacy projects (with no extension) and
 * document names used by the /file endpoint (with extension).
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Slf4j
public class LegacyFileMapper {

    @Inject
    private ProjectDAO projectDAO;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    /**
     *
     * @param projectSlug
     * @param iterationSlug
     * @param clientDocId
     * @param projectType
     * @return
     */
    String getServerDocId(String projectSlug, String iterationSlug,
            String clientDocId, @Nullable ProjectType projectType) {
        String suffix = getFilenameSuffix(projectSlug, iterationSlug, projectType, true);
        return StringUtils.removeEnd(clientDocId, suffix);
    }

    public String getFilenameSuffix(String projectSlug, String iterationSlug, @Nullable ProjectType clientProjectType, boolean forSourceFiles) {
        ProjectType projectType;
        // check the database first
        @Nullable ProjectType serverProjectType = getProjectType(projectSlug, iterationSlug);
        if (serverProjectType != null) {
            if (clientProjectType != null && serverProjectType != clientProjectType) {
                log.warn("server project type '{}' doesn't match client project type '{}' for project '{}' iteration '{}'",
                        serverProjectType, clientProjectType, projectSlug, iterationSlug);
            }
            projectType = serverProjectType;
        } else {
            if (clientProjectType != null) {
                // otherwise use the client's projectType
                projectType = clientProjectType;
                log.info("server project type 'null' overridden by client project type '{}' for project '{}' iteration '{}'",
                        clientProjectType, projectSlug, iterationSlug);
            } else {
                throw new WebApplicationException(
                        "Unknown project type: please update your Zanata client, or ask project maintainer to set the project type");
            }
        }
        return getFilenameSuffix(projectType, forSourceFiles);
    }

    public @Nullable ProjectType getProjectType(String projectSlug, String iterationSlug) {
        HProject project = projectDAO.getBySlug(projectSlug);
        if (project == null) {
            throw new WebApplicationException("Unknown project", 404);
        }
        HProjectIteration iteration =
                projectIterationDAO.getBySlug(project, iterationSlug);
        if (iteration == null) {
            throw new WebApplicationException("Unknown iteration", 404);
        }
        if (iteration.getProjectType() != null) {
            return iteration.getProjectType();
        }
        return project.getDefaultProjectType();
    }

    public @Nonnull String getFilenameSuffix(@Nonnull ProjectType projectType,
            boolean forSourceFiles) {
        // Get the file extension used by the client for old project types
        switch (projectType) {
            case Utf8Properties:
            case Properties:
                return ".properties";
            case Gettext:
            case Podir:
                return forSourceFiles ? ".pot" : ".po";
            case Xliff:
            case Xml:
                // NB the client used .xml for XLIFF projects, not .xlf
                return ".xml";
            case File:
                return "";
            default:
                throw new RuntimeException("Unhandled project type");
        }
    }

}
