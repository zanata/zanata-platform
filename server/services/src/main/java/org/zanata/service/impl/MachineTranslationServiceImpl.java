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
package org.zanata.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.config.MTServiceToken;
import org.zanata.config.MTServiceURL;
import org.zanata.config.MTServiceUser;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlow;
import org.zanata.service.MachineTranslationService;
import org.zanata.util.UrlUtil;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class MachineTranslationServiceImpl implements
        MachineTranslationService {
    private static final Logger log =
            LoggerFactory.getLogger(MachineTranslationServiceImpl.class);

    private URI mtServiceURL;
    private String mtUser;
    private String mtToken;
    private UrlUtil urlUtil;

    public MachineTranslationServiceImpl() {
    }

    @Inject
    public MachineTranslationServiceImpl(@MTServiceURL URI mtServiceURL,
            @MTServiceUser String mtUser,
            @MTServiceToken String mtToken,
            UrlUtil urlUtil) {
        this.mtServiceURL = mtServiceURL;
        this.mtUser = mtUser;
        this.mtToken = mtToken;
        this.urlUtil = urlUtil;
    }

    @Override
    public List<String> getSuggestion(@Nonnull HTextFlow textFlow,
            @Nonnull LocaleId fromLocale,
            @Nonnull LocaleId toLocale) {
        ResteasyClient client = new ResteasyClientBuilder().build();

        ResteasyWebTarget webTarget =
                client.target(mtServiceURL).path("api").path("document")
                        .path("translate")
                        .queryParam("toLocaleCode", toLocale.getId());

        MTDocument doc = new MTDocument();
        HDocument document = textFlow.getDocument();
        String docId = document.getDocId();
        String versionSlug = document.getProjectIteration().getSlug();
        String projectSlug = document.getProjectIteration().getProject().getSlug();

        doc.url = urlUtil.restPath("project/p/" + projectSlug +"/iterations/i" + versionSlug + "/resource?docId=" + docId);
        doc.backendId = "ms";
        doc.localeCode = fromLocale.getId();
        doc.contents = textFlow.getContents()
                .stream().map(TypeString::new).collect(Collectors.toList());

        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Auth-User", mtUser)
                .header("X-Auth-Token", mtToken)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(Entity.json(doc));



        if (response.getStatus() == 200) {
            MTDocument result = response.readEntity(MTDocument.class);
            if (result.getWarnings().isEmpty()) {
                return result.getContents().stream().map(TypeString::getValue)
                        .collect(Collectors.toList());
            } else {
                log.warn("Machine translation returns warning: {}",result.getWarnings());
            }
        }
        return Collections.emptyList();
    }

// TODO below DTOs duplicates what mt has. Should consider publish mt/common module
    static class MTDocument {
        private String url;
        private List<TypeString> contents = Lists.newArrayList();
        private String localeCode;
        private String backendId;
        private List<APIResponse> warnings = Lists.newArrayList();

        public String getUrl() {
            return url;
        }

        public List<TypeString> getContents() {
            return contents;
        }

        public String getLocaleCode() {
            return localeCode;
        }

        public String getBackendId() {
            return backendId;
        }

    public List<APIResponse> getWarnings() {
        return warnings;
    }
}

    static class TypeString {
        private String value;
        private String type = "text/plain";
        private String metadata = "";

        public TypeString(String value) {
            this.value = value;
        }

        public TypeString() {
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public String getMetadata() {
            return metadata;
        }
    }

    static class APIResponse {
        private int status;
        private String title;
        private String details;
        private String timestamp;

        public int getStatus() {
            return status;
        }

        public String getTitle() {
            return title;
        }

        public String getDetails() {
            return details;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("status", status)
                    .add("title", title)
                    .add("details", details)
                    .add("timestamp", timestamp)
                    .toString();
        }
    }
}
