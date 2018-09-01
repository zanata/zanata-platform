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

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.zanata.async.AsyncTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.config.MTServiceURL;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.MachineTranslationPrefill;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckRole;
import org.zanata.service.MachineTranslationService;
import org.zanata.util.HttpUtil;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("mt")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MachineTranslationResource {

    private DocumentDAO documentDAO;
    private TextFlowDAO textFlowDAO;
    private MachineTranslationService machineTranslationService;
    private ActiveProjectVersionAndLocaleValidator activeProjectVersionAndLocaleValidator;
    private ZanataIdentity identity;
    private MachineTranslationsManager machineTranslationsManager;
    private URI mtServiceURL;
    @Context
    private UriInfo uri;

    @Inject
    public MachineTranslationResource(DocumentDAO documentDAO,
            TextFlowDAO textFlowDAO,
            MachineTranslationService machineTranslationService,
            ActiveProjectVersionAndLocaleValidator activeProjectVersionAndLocaleValidator,
            ZanataIdentity identity,
            MachineTranslationsManager machineTranslationsManager,
            @MTServiceURL URI mtServiceURL) {
        this.documentDAO = documentDAO;
        this.textFlowDAO = textFlowDAO;
        this.machineTranslationService = machineTranslationService;
        this.activeProjectVersionAndLocaleValidator =
                activeProjectVersionAndLocaleValidator;
        this.identity = identity;
        this.machineTranslationsManager = machineTranslationsManager;
        this.mtServiceURL = mtServiceURL;
    }

    @SuppressWarnings("unused")
    public MachineTranslationResource() {
    }

    @Path("project/{projectSlug}/version/{versionSlug}")
    @GET
    @CheckRole("mt-suggestion")
    public Response getMachineTranslationSuggestion(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @QueryParam("docId") String docId,
            @QueryParam("resId") String resId,
            @QueryParam("toLocale") String toLocale) {
        if (mtServiceURL == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
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
        List<String> suggestion = machineTranslationService
                .getSuggestion(textFlow, doc.getSourceLocaleId(),
                        new LocaleId(toLocale));
        GenericEntity<List<String>> entity =
                new GenericEntity<List<String>>(suggestion) {
                };
        return Response.ok(entity).build();

    }

    @CheckRole("mt-bulk")
    @POST
    @Path("project/{projectSlug}/version/{versionSlug}")
    public Response prefillProjectVersionByMachineTranslation(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            MachineTranslationPrefill prefillRequest) {
        if (mtServiceURL == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        Optional<Response> response = activeProjectVersionAndLocaleValidator
                .getResponseIfProjectLocaleAndVersionAreNotActive(projectSlug,
                        versionSlug, prefillRequest.getToLocale());

        if (response.isPresent()) {
            return response.get();
        }

        identity.checkPermission("modify-translation",
                activeProjectVersionAndLocaleValidator.getProject(),
                activeProjectVersionAndLocaleValidator.getLocale());

        AsyncTaskHandle<Void> handle = machineTranslationsManager
                .prefillVersionWithMachineTranslations(
                        activeProjectVersionAndLocaleValidator.getVersion(),
                        prefillRequest);

        String url = uri.getBaseUri() + "process/key?keyId=" + handle.getKeyId();
        ProcessStatus processStatus = AsyncProcessService
                .handleToProcessStatus(handle, HttpUtil.stripProtocol(url));
        return Response.accepted(processStatus).build();
    }

    @CheckRole("mt-bulk")
    @POST
    @Path("project/{projectSlug}/version/{versionSlug}/document/{docId}")
    public Response prefillDocumentByMachineTranslation(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug,
            @PathParam("docId") String docId,
            MachineTranslationPrefill prefillRequest) {
        if (mtServiceURL == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        HDocument doc = documentDAO.getByProjectIterationAndDocId(projectSlug,
                versionSlug, docId);
        if (doc == null) {
            throw new NoSuchEntityException(
                    "can not find document with docId:" + docId + " in project "
                            + projectSlug + " and version " + versionSlug);
        }

        Optional<Response> response = activeProjectVersionAndLocaleValidator
                .getResponseIfProjectLocaleAndVersionAreNotActive(projectSlug,
                        versionSlug, prefillRequest.getToLocale());

        if (response.isPresent()) {
            return response.get();
        }

        identity.checkPermission("modify-translation",
                activeProjectVersionAndLocaleValidator.getProject(),
                activeProjectVersionAndLocaleValidator.getLocale());

        AsyncTaskHandle<Void> handle = machineTranslationsManager
                .prefillDocumentWithMachineTranslations(doc.getId(),
                        activeProjectVersionAndLocaleValidator.getVersion(),
                        prefillRequest);

        return Response.accepted(AsyncProcessService
                .handleToProcessStatus(handle, HttpUtil.stripProtocol(
                        uri.getBaseUri() + "process/key?keyId=" + handle.getKeyId())))
                .build();
    }
}
