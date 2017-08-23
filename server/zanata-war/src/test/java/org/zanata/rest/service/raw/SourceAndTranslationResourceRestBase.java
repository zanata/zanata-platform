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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
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

/**
 * Common parent for org.zanata.rest.service.raw.ResourceServiceRestITCase and
 * org.zanata.rest.service.raw.TranslationResourceRestITCase.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class SourceAndTranslationResourceRestBase extends RestTest {
    protected static final String DEPRECATED_BASE_PATH =
            "/projects/p/sample-project/iterations/i/1.0/r/";
    protected static final String BASE_PATH =
            "/projects/p/sample-project/iterations/i/1.0/resource";
    private SourceDocClient sourceDocResource;
    private TranslatedDocClient translatedDocResource;

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
        return RawRestTestUtils.jaxbUnmarshal(response, Resource.class);
    }

    public static ResourceMeta getResourceMetaFromResponse(
            Response resourceGetResponse) {
        return RawRestTestUtils.jaxbUnmarshal(
                resourceGetResponse, ResourceMeta.class);
    }

    public static TranslationsResource getTranslationsResourceFromResponse(
            Response response) {
        return jaxbUnmarshal(response, TranslationsResource.class);
    }

    public SourceDocClient getSourceDocResource() {
        if (sourceDocResource == null) {
            sourceDocResource = new SourceDocClient();
        }
        return sourceDocResource;
    }

    public TranslatedDocClient getTransResource() {
        if (translatedDocResource == null) {
            translatedDocResource = new TranslatedDocClient();
        }
        return translatedDocResource;
    }

    public class SourceDocClient implements SourceDocResource {
        @Override
        public Response head() {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH),
                    "HEAD", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget.request();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response get(final Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .request().header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_JSON);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response post(final Resource resource,
                final Set<String> extensions,
                @DefaultValue("true") final boolean copytrans) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH),
                    "POST", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("copyTrans",
                                    String.valueOf(copytrans))
                            .request();
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(resource),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPost(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        @Deprecated
        public Response getResource(String idNoSlash,
                final Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH + idNoSlash),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget).
                            request().header(HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response getResourceWithDocId(String docId,
                Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(BASE_PATH),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget).
                            queryParam("docId", docId).
                            request().header(HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response putResource(String idNoSlash,
                final Resource resource,
                final Set<String> extensions,
                @DefaultValue("true") final boolean copytrans) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH + idNoSlash),
                    "PUT", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("copyTrans",
                                    String.valueOf(copytrans)).request();
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(resource),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPut(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response putResourceWithDocId(Resource resource,
                String docId,
                Set<String> extensions, boolean copytrans) {
            return new ResourceRequest(
                    getRestEndpointUrl(BASE_PATH),
                    "PUT", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("docId", docId)
                            .queryParam("copyTrans",
                                    String.valueOf(copytrans)).request();
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(resource),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPut(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        @Deprecated
        public Response deleteResource(String idNoSlash) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH + idNoSlash),
                    "DELETE", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget.request();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response deleteResourceWithDocId(String docId) {
            return new ResourceRequest(getRestEndpointUrl(BASE_PATH),
                    "DELETE", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget.queryParam("docId", docId).request();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        @Deprecated
        public Response getResourceMeta(String idNoSlash,
                final Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(
                            DEPRECATED_BASE_PATH + idNoSlash + "/meta"),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .request().header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response getResourceMetaWithDocId(String docId,
                Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(
                            BASE_PATH + "/meta"),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("docId", docId)
                            .request().header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response putResourceMeta(String idNoSlash,
                final ResourceMeta messageBody,
                final Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(
                            DEPRECATED_BASE_PATH + idNoSlash + "/meta"),
                    "PUT", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .request();
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(messageBody),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPut(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response putResourceMetaWithDocId(ResourceMeta messageBody,
                String docId, Set<String> extensions) {
            return new ResourceRequest(
                    getRestEndpointUrl(
                            BASE_PATH + "/meta"),
                    "PUT", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("docId", docId)
                            .request();
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(messageBody),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPut(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }
    }

    public class TranslatedDocClient implements TranslatedDocResource {

        @Override
        public Response getTranslations(String idNoSlash, LocaleId locale,
                final Set<String> extensions, final boolean createSkeletons,
                String eTag) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH + idNoSlash + "/translations/" + locale),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("skeletons",
                                    String.valueOf(createSkeletons))
                            .request().header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response getTranslationsWithDocId(LocaleId locale,
                String docId,
                Set<String> extensions, boolean createSkeletons,
                String eTag) {
            return new ResourceRequest(
                    getRestEndpointUrl(BASE_PATH + "/translations/" + locale),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("docId", docId)
                            .queryParam("skeletons",
                                    String.valueOf(createSkeletons))
                            .request().header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response deleteTranslations(String idNoSlash,
                LocaleId locale) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH + idNoSlash + "/translations/" + locale),
                    "DELETE", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget.request();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response deleteTranslationsWithDocId(LocaleId locale,
                String docId) {
            return new ResourceRequest(
                    getRestEndpointUrl(BASE_PATH + "/translations/" + locale),
                    "DELETE", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget.queryParam("docId", docId).request();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response putTranslations(String idNoSlash, LocaleId locale,
                final TranslationsResource messageBody, final Set<String> extensions,
                @DefaultValue("auto") final String merge) {
            return new ResourceRequest(
                    getRestEndpointUrl(DEPRECATED_BASE_PATH + idNoSlash + "/translations/" + locale),
                    "PUT", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("merge", merge).request()
                            .header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(messageBody),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPut(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }

        @Override
        public Response putTranslationsWithDocId(LocaleId locale,
                TranslationsResource messageBody, String docId,
                Set<String> extensions,
                String merge) {
            return new ResourceRequest(
                    getRestEndpointUrl(BASE_PATH + "/translations/" + locale),
                    "PUT", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return addExtensionToRequest(extensions, webTarget)
                            .queryParam("docId", docId)
                            .queryParam("merge", merge).request()
                            .header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                public Response invokeWithResponse(
                        Invocation.Builder builder) {
                    Entity<String> entity = Entity
                            .entity(jaxbMarhsal(messageBody),
                                    MediaType.APPLICATION_XML_TYPE);
                    return builder.buildPut(entity).invoke();
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();
        }
    }
}
