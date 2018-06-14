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

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.service.MachineTranslationService;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("mt")
@Produces(MediaType.APPLICATION_JSON)
public class MachineTranslationResource {

    private DocumentDAO documentDAO;
    private TextFlowDAO textFlowDAO;
    private MachineTranslationService machineTranslationService;

    @Inject
    public MachineTranslationResource(DocumentDAO documentDAO,
            TextFlowDAO textFlowDAO,
            MachineTranslationService machineTranslationService) {
        this.documentDAO = documentDAO;
        this.textFlowDAO = textFlowDAO;
        this.machineTranslationService = machineTranslationService;
    }

    protected MachineTranslationResource() {
    }

    @Path("project/{projectSlug}/version/{versionSlug}")
    @GET
    public List<String> getMachineTranslationSuggestion(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @QueryParam("docId") String docId,
            @QueryParam("resId") String resId,
            @QueryParam("toLocale") String toLocale) {
        HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug,
                versionSlug, docId);

        if (doc == null) {
            throw new NoSuchEntityException(
                    "can not find document with docId:" + docId + " in project "
                            + projectSlug + " and version " + versionSlug);
        }
        HTextFlow textFlow = textFlowDAO.getById(doc, resId);
        if (textFlow == null) {
            throw new NoSuchEntityException("can not find text flow with resId:" + resId + " in document " + docId);
        }
        return machineTranslationService.getSuggestion(textFlow, doc.getSourceLocaleId(), new LocaleId(toLocale));

    }
}
