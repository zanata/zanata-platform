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
package org.zanata.rest.editor.service;

import javaslang.collection.HashMap;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.rest.editor.service.resource.TransUnitHistoryResource;
import org.zanata.webtrans.server.rpc.GetTranslationHistoryHandler;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;
import org.zanata.rest.service.RestUtils;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author Earl Floden <a href="mailto:efloden@redhat.com">efloden@redhat.com</a>
 */
@RequestScoped
@Path(TransUnitHistoryResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class TransUnitHistoryService implements TransUnitHistoryResource {

    @Inject @Any
    private GetTranslationHistoryHandler historyHandler;

    @SuppressWarnings("unused")
    public TransUnitHistoryService() {
    }

    @java.beans.ConstructorProperties({"historyHandler"})
    protected TransUnitHistoryService(
            final GetTranslationHistoryHandler historyHandler
    ) {
        this.historyHandler = historyHandler;
    }

    @Override
    public Response get(String localeId, Long transUnitId, String projectSlug,
            String versionSlug) {

        HashMap<Object, String> params =
                HashMap.of(localeId, "localeId", projectSlug, "projectSlug",
                        versionSlug, "versionSlug", transUnitId, "transUnitId");
        Response error = RestUtils.checkParams(params);
        if (error != null) return error;
        GetTranslationHistoryResult result =
                historyHandler.getTranslationHistory(
                        localeId, transUnitId, projectSlug, versionSlug);
        return Response.ok(result).build();
    }
}
