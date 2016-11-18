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
class LegacyFileMapper {

    String getServerDocId(@Nullable ProjectType serverProjectType,
            String clientDocId, @Nullable ProjectType clientProjectType) {
        String suffix = getFilenameSuffix(serverProjectType, clientProjectType, true);
        return StringUtils.removeEnd(clientDocId, suffix);
    }

    private String getFilenameSuffix(@Nullable ProjectType serverProjectType,
            @Nullable ProjectType clientProjectType, boolean forSourceFiles) {
        if (serverProjectType == ProjectType.File) {
            // for File projects, clientProjectType is irrelevant because we store the real extension
            return getFilenameSuffix(ProjectType.File, forSourceFiles);
        }
        // otherwise use the client's projectType
        if (serverProjectType != null) {
            if (clientProjectType != null && serverProjectType != clientProjectType) {
                log.warn("Server project type '{}' doesn't match client project type '{}'",
                        serverProjectType, clientProjectType);
            }
        } else {
            if (clientProjectType != null) {
                log.info("Server project type 'null' overridden by client project type '{}'",
                        clientProjectType);
                // TODO we should store project type (but only if maintainer pushing source files)
            } else {
                throw new WebApplicationException(
                        "Unknown project type: please ask project maintainer to set the project type (or add project type to your client config)");
            }
        }
        // we want to remove the client project type's extension from the client's filename
        return getFilenameSuffix(clientProjectType, forSourceFiles);
    }

    @Nonnull
    private String getFilenameSuffix(@Nonnull ProjectType projectType,
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
