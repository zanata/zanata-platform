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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.SourceLocaleDetails;
import org.zanata.service.LocaleService;

@RequestScoped
@Named("projectLocalesService")
@Path(ProjectLocalesResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class ProjectLocalesService implements ProjectLocalesResource {
    @PathParam("projectSlug")
    String projectSlug;

    @Inject
    private ProjectDAO projectDAO;

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public Response get() {
        HProject project = projectDAO.getBySlug(projectSlug);
        if (project == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<HLocale> supportedLocales =
                localeServiceImpl.getSupportedLanguageByProject(projectSlug);
        Map<LocaleId, String> localeAliases = project.getLocaleAliases();

        Object entity = LocaleService.buildLocaleDetailsListEntity(supportedLocales, localeAliases);
        return Response.ok(entity).build();
    }

    @Override
    public Response getSourceLocales() {
        HProject project = projectDAO.getBySlug(projectSlug);
        if (project == null || project.getStatus().equals(EntityStatus.OBSOLETE)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<HLocale, Integer> locales =
                localeDAO.getProjectSourceLocalesAndDocCount(projectSlug);
        List<SourceLocaleDetails> results = new ArrayList<>();

        for (Map.Entry<HLocale, Integer> entry: locales.entrySet()) {
            LocaleDetails details = LocaleService.convertHLocaleToDTO(entry.getKey());
            results.add(new SourceLocaleDetails(entry.getValue(), details));
        }
        if (results.size() > 1) {
            // Adding total doc count to the result set
            int count = projectDAO.getTotalDocCount(projectSlug);
            results.add(new SourceLocaleDetails(count, null));
        }
        return Response
                .ok(new GenericEntity<List<SourceLocaleDetails>>(results) {
                }).build();
    }
}
