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
package org.zanata.rest.service.raw;

import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;

import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.zanata.RestTest;
import org.zanata.common.LocaleId;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.SourceDocResource;
import org.zanata.rest.service.TranslatedDocResource;
import org.zanata.util.RawRestTestUtils;

import com.google.common.collect.Lists;

/**
 * Common parent for org.zanata.rest.service.raw.ResourceServiceRestITCase and
 * org.zanata.rest.service.raw.TranslationResourceRestTest.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class SourceAndTranslationResourceRestBase extends RestTest {
    protected static final String RESOURCE_PATH =
            "/projects/p/sample-project/iterations/i/1.0/r/";
    private SourceDocResource sourceDocResource;
    private TranslatedDocResource translatedDocResource;

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    public static Resource getResourceFromResponse(Response response) {
        return RawRestTestUtils.jaxbUnmarshal(
                (ClientResponse) response, Resource.class);
    }

    public static ResourceMeta getResourceMetaFromResponse(
            Response resourceGetResponse) {
        return RawRestTestUtils.jaxbUnmarshal(
                (ClientResponse) resourceGetResponse,
                ResourceMeta.class);
    }

    public static TranslationsResource getTranslationsResourceFromResponse(
            Response response) {
        return jaxbUnmarshal((ClientResponse) response, TranslationsResource.class);
    }

    public SourceDocResource getSourceDocResource() {
        if (sourceDocResource == null) {
            sourceDocResource = new SourceDocResource() {
                @Override
                public Response head() {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH),
                            "HEAD", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response get(final Set<String> extensions) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                            request.header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_JSON);
                            addExtensionToRequest(extensions, request);
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response post(final Resource resource,
                        final Set<String> extensions,
                        @DefaultValue("true") final boolean copytrans) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH),
                            "POST", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                            addExtensionToRequest(extensions, request);
                            request.queryParameter("copyTrans",
                                    String.valueOf(copytrans));
                            request.body(MediaType.APPLICATION_XML_TYPE,
                                    jaxbMarhsal(
                                            resource));
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response getResource(String idNoSlash,
                        final Set<String> extensions) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH + idNoSlash),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                            addExtensionToRequest(extensions, request);
                            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_TYPE);
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response putResource(String idNoSlash,
                        final Resource resource,
                        final Set<String> extensions,
                        @DefaultValue("true") final boolean copytrans) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH + idNoSlash),
                            "PUT", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_TYPE);
                            addExtensionToRequest(extensions, request);
                            request.queryParameter("copyTrans",
                                    String.valueOf(copytrans));
                            request.body(MediaType.APPLICATION_XML_TYPE,
                                    jaxbMarhsal(
                                            resource));
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response deleteResource(String idNoSlash) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH + idNoSlash),
                            "DELETE", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response getResourceMeta(String idNoSlash,
                        final Set<String> extensions) {
                    return new ResourceRequest(
                            getRestEndpointUrl(
                                    RESOURCE_PATH + idNoSlash + "/meta"),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_TYPE);
                            addExtensionToRequest(extensions, request);
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response putResourceMeta(String idNoSlash,
                        final ResourceMeta messageBody,
                        final Set<String> extensions) {
                    return new ResourceRequest(
                            getRestEndpointUrl(
                                    RESOURCE_PATH + idNoSlash + "/meta"),
                            "PUT", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(
                                ClientRequest request) {
                            addExtensionToRequest(extensions, request);
                            request.body(MediaType.APPLICATION_XML_TYPE,
                                    jaxbMarhsal(messageBody));
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }
            };
        }
        return sourceDocResource;
    }

    public TranslatedDocResource getTransResource() {
        if (translatedDocResource == null) {
            translatedDocResource = new TranslatedDocResource() {
                @Override
                public Response getTranslations(String idNoSlash, LocaleId locale,
                        final Set<String> extensions, final boolean createSkeletons,
                        String eTag) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH + idNoSlash + "/translations/" + locale),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(ClientRequest request) {
                            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_TYPE);
                            addExtensionToRequest(extensions, request);
                            request.queryParameter("skeletons",
                                    String.valueOf(createSkeletons));
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response deleteTranslations(String idNoSlash,
                        LocaleId locale) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH + idNoSlash + "/translations/" + locale),
                            "DELETE", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(ClientRequest request) {
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response putTranslations(String idNoSlash, LocaleId locale,
                        final TranslationsResource messageBody, final Set<String> extensions,
                        @DefaultValue("auto") final String merge) {
                    return new ResourceRequest(
                            getRestEndpointUrl(RESOURCE_PATH + idNoSlash + "/translations/" + locale),
                            "PUT", getAuthorizedEnvironment()) {
                        @Override
                        protected void prepareRequest(ClientRequest request) {
                            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_TYPE);
                            addExtensionToRequest(extensions, request);
                            request.queryParameter("merge", merge);
                            request.body(MediaType.APPLICATION_XML_TYPE, jaxbMarhsal(messageBody));
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }
            };
        }
        return translatedDocResource;
    }
}
