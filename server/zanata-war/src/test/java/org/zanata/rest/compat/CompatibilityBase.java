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
package org.zanata.rest.compat;

import java.io.IOException;
import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.zanata.RestTest;
import org.zanata.apicompat.common.LocaleId;
import org.zanata.apicompat.rest.MediaTypes;
import org.zanata.apicompat.rest.dto.Account;
import org.zanata.apicompat.rest.dto.resource.Resource;
import org.zanata.apicompat.rest.dto.resource.ResourceMeta;
import org.zanata.apicompat.rest.dto.resource.TranslationsResource;
import org.zanata.apicompat.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.apicompat.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.apicompat.rest.service.AccountResource;
import org.zanata.apicompat.rest.service.SourceDocResource;
import org.zanata.apicompat.rest.service.StatisticsResource;
import org.zanata.apicompat.rest.service.TranslatedDocResource;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.ResourceRequestEnvironment;

import com.google.common.collect.Lists;

import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class CompatibilityBase extends RestTest {

    protected void releaseConnection(Response response) throws IOException {
        if (response instanceof ClientResponse) {
            ((ClientResponse) response).releaseConnection();
        }
    }

    protected Resource getResourceFromResponse(Response response) {
        return jaxbUnmarshal(response, Resource.class);
    }

    protected ResourceMeta getResourceMetaFromResponse(Response response) {
        return jaxbUnmarshal(response, ResourceMeta.class);
    }

    protected TranslationsResource getTransResourceFromResponse(Response response) {
        return jaxbUnmarshal(response, TranslationsResource.class);
    }

    public SourceDocResource getSourceDocResource(final String resourcePath) {
        return new SourceDocResource() {
                @Override
                public Response head() {
                    return new ResourceRequest(
                            getRestEndpointUrl(resourcePath),
                            "HEAD", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return webTarget.request();
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response get(final Set<String> extensions) {
                    return new ResourceRequest(
                            getRestEndpointUrl(resourcePath),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).request()
                                    .header(HttpHeaders.ACCEPT,
                                            MediaType.APPLICATION_JSON);
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
                            getRestEndpointUrl(resourcePath),
                            "POST", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).queryParam("copyTrans",
                                    String.valueOf(copytrans)).request();
                        }

                        @Override
                        public ClientResponse invokeWithResponse(
                                Invocation.Builder builder) {
                            Entity entity = Entity
                                    .entity(jaxbMarhsal(resource),
                                            MediaType.APPLICATION_XML_TYPE);
                            Response response = builder.buildPost(entity)
                                    .invoke();
                            return (ClientResponse) response;
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
                            getRestEndpointUrl(resourcePath + idNoSlash),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).request()
                                    .header(HttpHeaders.ACCEPT,
                                            MediaType.APPLICATION_XML_TYPE);
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
                            getRestEndpointUrl(resourcePath + idNoSlash),
                            "PUT", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).queryParam("copyTrans",
                                    String.valueOf(copytrans)).request()
                                    .header(HttpHeaders.ACCEPT,
                                            MediaType.APPLICATION_XML_TYPE);
                        }

                        @Override
                        public ClientResponse invokeWithResponse(
                                Invocation.Builder builder) {
                            Entity entity = Entity
                                    .entity(jaxbMarhsal(resource),
                                            MediaType.APPLICATION_XML_TYPE);
                            return (ClientResponse) builder.buildPut(entity)
                                    .invoke();
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }

                @Override
                public Response deleteResource(String idNoSlash) {
                    return new ResourceRequest(
                            getRestEndpointUrl(resourcePath + idNoSlash),
                            "DELETE", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return webTarget.request();
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
                                    resourcePath + idNoSlash + "/meta"),
                            "GET", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).request()
                                    .header(HttpHeaders.ACCEPT,
                                            MediaType.APPLICATION_XML_TYPE);
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
                                    resourcePath + idNoSlash + "/meta"),
                            "PUT", getAuthorizedEnvironment()) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).request();
                        }

                        @Override
                        public ClientResponse invokeWithResponse(
                                Invocation.Builder builder) {
                            Entity entity = Entity
                                    .entity(jaxbMarhsal(messageBody),
                                            MediaType.APPLICATION_XML_TYPE);
                            return (ClientResponse) builder.buildPut(entity)
                                    .invoke();
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }
            };

    }

    public TranslatedDocResource getTransResource(final String resourcePath,
            final AuthenticatedAsUser authenticatedAsUser) {
        return new TranslatedDocResource() {
                @Override
                public Response getTranslations(String idNoSlash, LocaleId locale,
                        final Set<String> extensions, final boolean createSkeletons,
                        String eTag) {
                    return new ResourceRequest(
                            getRestEndpointUrl(resourcePath + idNoSlash + "/translations/" + locale),
                            "GET", authenticatedAsUser.authorizedEnv) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).queryParam("skeletons",
                                    String.valueOf(createSkeletons)).request()
                                    .header(HttpHeaders.ACCEPT,
                                            MediaType.APPLICATION_XML_TYPE);
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
                            getRestEndpointUrl(resourcePath + idNoSlash + "/translations/" + locale),
                            "DELETE", authenticatedAsUser.authorizedEnv) {
                        @Override
                        protected Invocation.Builder prepareRequest(
                                ResteasyWebTarget webTarget) {
                            return webTarget.request();
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
                            getRestEndpointUrl(resourcePath + idNoSlash + "/translations/" + locale),
                            "PUT", authenticatedAsUser.authorizedEnv) {
                        @Override
                        protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                            return addExtensionToRequest(extensions,
                                    webTarget).queryParam("merge", merge)
                                    .request().header(HttpHeaders.ACCEPT,
                                            MediaType.APPLICATION_XML_TYPE);
                        }

                        @Override
                        public ClientResponse invokeWithResponse(
                                Invocation.Builder builder) {
                            Entity entity = Entity
                                    .entity(jaxbMarhsal(messageBody),
                                            MediaType.APPLICATION_XML_TYPE);
                            return (ClientResponse) builder.buildPut(entity)
                                    .invoke();
                        }

                        @Override
                        protected void onResponse(ClientResponse response) {
                        }
                    }.runWithResult();
                }
            };
    }

    protected AccountResource getAccountResource(final String resourceUrl,
            final AuthenticatedAsUser authenticatedAsUser,
            final String mediaType) {

        return new AccountResource() {
            @Override
            public Response get() {
                return new ResourceRequest(getRestEndpointUrl(resourceUrl),
                        "GET", authenticatedAsUser.authorizedEnv) {
                    @Override
                    protected Invocation.Builder prepareRequest(
                            ResteasyWebTarget webTarget) {
                        return webTarget.request().header(HttpHeaders.ACCEPT,
                                mediaType);
                    }

                    @Override
                    protected void onResponse(ClientResponse response) {
                    }
                }.runWithResult();
            }

            @Override
            public Response put(final Account account) {
                return new ResourceRequest(getRestEndpointUrl(resourceUrl),
                        "PUT", authenticatedAsUser.authorizedEnv) {
                    @Override
                    protected Invocation.Builder prepareRequest(
                            ResteasyWebTarget webTarget) {
                        return webTarget.request().header(HttpHeaders.ACCEPT,
                                mediaType);
                    }

                    @Override
                    public ClientResponse invokeWithResponse(
                            Invocation.Builder builder) {
                        Entity entity = null;
                        if (MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON
                                .equals(mediaType)) {
                            entity = Entity
                                    .entity(jsonMarshal(account),
                                            MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
                        } else {
                            entity = Entity
                                    .entity(jaxbMarhsal(account),
                                            MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
                        }
                        return (ClientResponse) builder.buildPut(entity)
                                .invoke();
                    }

                    @Override
                    protected void onResponse(ClientResponse response) {
                    }
                }.runWithResult();
            }
        };
    }

    protected StatisticsResource getStatisticsResource() {
        return new StatisticsResource() {
            @Override
            public ContainerTranslationStatistics getStatistics(
                    String projectSlug,
                    String iterationSlug, final boolean includeDetails,
                    final boolean includeWordStats, final String[] locales) {
                ResourceRequest resourceRequest = new ResourceRequest(
                        getRestEndpointUrl(String.format("/stats/proj/%s/iter/%s",
                                        projectSlug, iterationSlug)),
                        "GET") {
                    @Override
                    protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                        webTarget = webTarget.queryParam("detail",
                                String.valueOf(includeDetails)).queryParam("word", String.valueOf(includeWordStats));
                        if (locales != null) {
                            webTarget = webTarget.queryParam("locale",
                                    Lists.newArrayList(locales));
                        }
                        return webTarget.request().header(HttpHeaders.ACCEPT,
                                MediaType.APPLICATION_XML);
                    }

                    @Override
                    protected void onResponse(ClientResponse response) {
                    }

                };
                return resourceRequest.runWithResult()
                        .readEntity(ContainerTranslationStatistics.class);
            }

            @Override
            public ContainerTranslationStatistics getStatistics(
                    String projectSlug,
                    String iterationSlug, String docId,
                    final boolean includeWordStats,
                    final String[] locales) {
                ResourceRequest resourceRequest = new ResourceRequest(
                        getRestEndpointUrl(String.format("/stats/proj/%s/iter/%s/doc/%s",
                                projectSlug, iterationSlug, docId)),
                        "GET") {
                    @Override
                    protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                        webTarget = webTarget.queryParam("word",
                                String.valueOf(includeWordStats));
                        if (locales != null) {
                            webTarget = webTarget.queryParam("locale",
                                    Lists.newArrayList(locales));
                        }
                        return webTarget.request().header(HttpHeaders.ACCEPT,
                                MediaType.APPLICATION_XML);
                    }

                    @Override
                    protected void onResponse(ClientResponse response) {
                    }

                };
                return resourceRequest.runWithResult()
                        .readEntity(ContainerTranslationStatistics.class);
            }

            @Override
            public ContributionStatistics getContributionStatistics(String projectSlug,
                    String versionSlug,
                    String username, String dateRange, @DefaultValue("false") boolean includeAutomatedEntry) {
                ResourceRequest resourceRequest = new ResourceRequest(
                        getRestEndpointUrl(String.format("/stats/project/%s/version/%s/contributor/%s/%s",
                                projectSlug, versionSlug, username, dateRange)),
                        "GET") {
                    @Override
                    protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                        return webTarget.queryParam("includeAutomatedEntry",
                                String.valueOf(includeAutomatedEntry)).request()
                                .header(HttpHeaders.ACCEPT,
                                        MediaType.APPLICATION_JSON);
                    }

                    @Override
                    protected void onResponse(ClientResponse response) {
                    }

                };
                return resourceRequest.runWithResult()
                        .readEntity(ContributionStatistics.class);
            }
        };
    }

    enum AuthenticatedAsUser {
        Admin(getAuthorizedEnvironment()), Translator(getTranslatorHeaders());

        private final ResourceRequestEnvironment authorizedEnv;

        AuthenticatedAsUser(ResourceRequestEnvironment authorizedEnv) {
            this.authorizedEnv = authorizedEnv;
        }
    }
}
