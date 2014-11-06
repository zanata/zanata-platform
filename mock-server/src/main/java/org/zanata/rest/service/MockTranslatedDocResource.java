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

import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(TranslatedDocResource.SERVICE_PATH)
public class MockTranslatedDocResource implements TranslatedDocResource {

    @Override
    public Response getTranslations(String idNoSlash, LocaleId locale,
            Set<String> extensions, boolean createSkeletons,
            @HeaderParam("If-None-Match") String eTag) {
        MockResourceUtil.validateExtensions(extensions);
        TranslationsResource transResource = new TranslationsResource();
        transResource.getTextFlowTargets().add(new TextFlowTarget(idNoSlash));
        return Response.ok(transResource).build();
    }

    @Override
    public Response deleteTranslations(String idNoSlash, LocaleId locale) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response putTranslations(String idNoSlash, LocaleId locale,
            TranslationsResource messageBody, Set<String> extensions,
            @DefaultValue("auto") String merge) {
        // used by PublicanPush only
        MockResourceUtil.validateExtensions(extensions);
        return Response.ok().build();
    }
}
