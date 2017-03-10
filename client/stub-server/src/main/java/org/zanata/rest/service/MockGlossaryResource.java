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

import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.zanata.common.LocaleId;
import org.zanata.rest.GlossaryFileUploadForm;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryResults;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(GlossaryResource.SERVICE_PATH)
public class MockGlossaryResource implements GlossaryResource {
    @Context
    UriInfo uriInfo;

    @Override
    public Response getInfo(String qualifiedName) {
        return Response.ok(GlossaryResource.GLOBAL_QUALIFIED_NAME).build();
    }

    @Override
    public Response getEntries(LocaleId srcLocale,
        LocaleId transLocale, int page, int sizePerPage, String filter,
        String sort, String qualifiedName) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response downloadFile(@DefaultValue("csv") String fileType,
            String locales, String qualifiedName) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response post(List<GlossaryEntry> glossaryEntries, String locale,
            String qualifiedName) {
        GlossaryResults results = new GlossaryResults();
        results.setGlossaryEntries(glossaryEntries);
        GenericEntity<GlossaryResults> genericEntity =
                new GenericEntity<GlossaryResults>(results) {
                };
        return Response.ok(genericEntity).build();
    }

    @Override
    public Response getQualifiedName() {
        return null;
    }

    @Override
    public Response upload(GlossaryFileUploadForm glossaryFileUploadForm) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response deleteEntry(Long id, String qualifiedName) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response deleteAllEntries(String qualifiedName) {
        return null;
    }
}

