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
package org.zanata.rest.editor.service

import cyclops.data.HashMap
import org.apache.deltaspike.jpa.api.transaction.Transactional
import org.zanata.rest.editor.service.resource.TransUnitHistoryResource
import org.zanata.rest.service.RestUtils
import org.zanata.webtrans.server.rpc.GetTranslationHistoryHandler
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.Path
import javax.ws.rs.core.Response

private typealias AnyQ = javax.enterprise.inject.Any

/**
 * @author Earl Floden [efloden@redhat.com](mailto:efloden@redhat.com)
 */
@RequestScoped
@Path(TransUnitHistoryResource.SERVICE_PATH)
@Transactional(readOnly = true)
class TransUnitHistoryService @Inject constructor(
        @AnyQ private val historyHandler: GetTranslationHistoryHandler)
    : TransUnitHistoryResource {

    override fun get(localeId: String, transUnitId: Long, projectSlug: String,
            versionSlug: String?): Response {
        val params = HashMap.empty<String, Any>()
                .put("localeId", localeId)
                .put("projectSlug", projectSlug)
                .put("versionSlug", versionSlug)
                .put("transUnitId", transUnitId)
        val error = RestUtils.checkParams(params)
        if (error != null) return error
        val result = historyHandler.getTranslationHistory(
                localeId, transUnitId, projectSlug, versionSlug)
        return Response.ok(result).build()
    }
}
