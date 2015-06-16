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

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(SourceDocResource.SERVICE_PATH)
public class MockSourceDocResource implements SourceDocResource {

    @Override
    public Response head() {
        return Response.ok(new EntityTag(new Date().toString())).build();
    }

    @Override
    public Response get(Set<String> extensions) {
        MockResourceUtil.validateExtensions(extensions);
        Collection<ResourceMeta> samples =
                new ResourceMeta("about-fedora").createSamples();
        GenericEntity<Collection<ResourceMeta>> entity =
                new GenericEntity<Collection<ResourceMeta>>(samples) {
                };
        return Response.ok(entity).build();
    }

    @Override
    public Response post(Resource resource, Set<String> extensions,
            @DefaultValue("true") boolean copyTrans) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response getResource(String idNoSlash, Set<String> extensions) {
        MockResourceUtil.validateExtensions(extensions);
        return Response.ok(new Resource(idNoSlash)).build();
    }

    @Override
    public Response putResource(String idNoSlash, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copyTrans) {
        MockResourceUtil.validateExtensions(extensions);
        return Response.ok(resource.getName()).build();
    }

    @Override
    public Response deleteResource(String idNoSlash) {
        return Response.ok().build();
    }

    @Override
    public Response getResourceMeta(String idNoSlash, Set<String> extensions) {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response putResourceMeta(String idNoSlash, ResourceMeta resourceMeta,
            Set<String> extensions) {
        return MockResourceUtil.notUsedByClient();
    }
}

