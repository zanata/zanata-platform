/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.rest.editor.service.resource.TransUnitHistoryResource;
import org.zanata.webtrans.server.rpc.GetTranslationHistoryHandler;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Earl Floden <a href="mailto:efloden@redhat.com">efloden@redhat.com</a>
 */
@RequestScoped
@Named("editor.transUnitHistoryService")
@Path(TransUnitHistoryResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class TransUnitHistoryService implements TransUnitHistoryResource {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TransUnitHistoryService.class);

    @Inject @Any
    private GetTranslationHistoryHandler historyHandler;

    @Override
    public Response get(String localeId, Long transUnitId) {
        if (isNullOrEmpty(localeId)) {
            String msg1 = String.format("Null or empty localeId supplied");
            log.warn(msg1);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg1)
                    .build();
        }
        if (transUnitId == null) {
            String msg2 = String.format("Null transUnitId supplied");
            log.warn(msg2);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg2)
                    .build();
        }
        GetTranslationHistoryResult result =
                historyHandler.getTranslationHistory(localeId, transUnitId);
        return Response.ok(result).build();
    }
}
