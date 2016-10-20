/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.LocaleServiceImpl;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Map;

@RequestScoped
@Named("projectIterationLocalesService")
@Path(ProjectIterationLocalesService.SERVICE_PATH)
@Transactional(readOnly = true)
public class ProjectIterationLocalesService extends LocalesService implements ProjectIterationLocalesResource {
    @PathParam("projectSlug")
    String projectSlug;

    @PathParam("iterationSlug")
    String iterationSlug;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public Response get() {
        HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

        if (iteration == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        List<HLocale> supportedLocales =
                localeServiceImpl.getSupportedLanguageByProjectIteration(projectSlug, iterationSlug);
        Map<LocaleId, String> localeAliases = LocaleServiceImpl.getLocaleAliasesByIteration(iteration);

        Object entity = buildLocaleDetailsListEntity(supportedLocales, localeAliases);
        return Response.ok(entity).build();
    }

}
