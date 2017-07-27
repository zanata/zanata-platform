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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.apicompat.rest.service.SourceDocResource;
import org.zanata.apicompat.rest.service.TranslatedDocResource;
import org.zanata.apicompat.common.ContentState;
import org.zanata.apicompat.common.ContentType;
import org.zanata.apicompat.common.LocaleId;
import org.zanata.apicompat.common.ResourceType;
import org.zanata.apicompat.rest.StringSet;
import org.zanata.apicompat.rest.dto.extensions.comment.SimpleComment;
import org.zanata.apicompat.rest.dto.extensions.gettext.PoHeader;
import org.zanata.apicompat.rest.dto.resource.Resource;
import org.zanata.apicompat.rest.dto.resource.ResourceMeta;
import org.zanata.apicompat.rest.dto.resource.TextFlow;
import org.zanata.apicompat.rest.dto.resource.TextFlowTarget;
import org.zanata.apicompat.rest.dto.resource.TranslationsResource;
import org.zanata.rest.ResourceRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class TranslationsRawCompatibilityITCase extends CompatibilityBase {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/DocumentsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void getJsonResource() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, Resource.class);

                Resource resource = jsonUnmarshal(entityString, Resource.class);
                assertThat(resource.getName()).isEqualTo("my/path/document-2.txt");
                assertThat(resource.getType()).isEqualTo(ResourceType.FILE);
                assertThat(resource.getLang()).isEqualTo(LocaleId.EN_US);

                // Make sure all Text Flows are present
                assertThat(resource.getTextFlows().size()).isGreaterThanOrEqualTo(3);

                // Evaluate individual text flows
                TextFlow txtFlow = resource.getTextFlows().get(0);
                assertThat(txtFlow.getId()).isEqualTo("tf2");
                assertThat(txtFlow.getRevision()).isEqualTo(1);
                assertThat(txtFlow.getContents().get(0)).isEqualTo("mssgId1");

                txtFlow = resource.getTextFlows().get(1);
                assertThat(txtFlow.getId()).isEqualTo("tf3");
                assertThat(txtFlow.getRevision()).isEqualTo(1);
                assertThat(txtFlow.getContents().get(0)).isEqualTo("mssgId2");

                txtFlow = resource.getTextFlows().get(2);
                assertThat(txtFlow.getId()).isEqualTo("tf4");
                assertThat(txtFlow.getRevision()).isEqualTo(1);
                assertThat(txtFlow.getContents().get(0)).isEqualTo("mssgId3");
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void postJsonResource() throws Exception {
        // Create a new Resource
        final Resource res = new Resource("new-resource");
        res.setType(ResourceType.FILE);
        res.setContentType(ContentType.TextPlain);
        res.setLang(LocaleId.EN_US);
        res.setRevision(1);
        res.getExtensions(true).add(new PoHeader("This is a PO Header"));

        TextFlow tf1 = new TextFlow("tf1", LocaleId.EN_US, "First Text Flow");
        tf1.getExtensions(true).add(new SimpleComment("This is one comment"));
        res.getTextFlows().add(tf1);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r"),
                "POST", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", PoHeader.ID).queryParam(
                        "ext", SimpleComment.ID).request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(res), MediaType.APPLICATION_JSON);
                Response response = builder.buildPost(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // 201
                assertThat(response.getStatus())
                        .isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();

        // Verify that it was created successfully
        SourceDocResource sourceDocClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        Response resourceResponse =
                sourceDocClient.getResource(res.getName(), new StringSet(
                        PoHeader.ID + ";" + SimpleComment.ID));

        // 200
        assertThat(resourceResponse.getStatus()).isEqualTo(Status.OK.getStatusCode());

        Resource createdResource = getResourceFromResponse(resourceResponse);

        assertThat(createdResource.getName()).isEqualTo(res.getName());
        assertThat(createdResource.getType()).isEqualTo(res.getType());
        assertThat(createdResource.getContentType()).isEqualTo(res.getContentType());
        assertThat(createdResource.getLang()).isEqualTo(res.getLang());
        // Created, so revision 1
        assertThat(createdResource.getRevision()).isEqualTo(1);

        // Extensions
        assertThat(createdResource.getExtensions(true).size()).isGreaterThanOrEqualTo(1);
        assertThat(
                createdResource.getExtensions(true).findByType(PoHeader.class)
                        .getComment()).isEqualTo("This is a PO Header");

        // Text Flow
        assertThat(createdResource.getTextFlows().size()).isEqualTo(1);

        TextFlow createdTf = createdResource.getTextFlows().get(0);
        assertThat(createdTf.getContents()).isEqualTo(tf1.getContents());
        assertThat(createdTf.getId()).isEqualTo(tf1.getId());
        assertThat(createdTf.getLang()).isEqualTo(tf1.getLang());
        // Create, so revision 1
        assertThat(createdTf.getRevision()).isEqualTo(1);

        // Text Flow extensions
        assertThat(createdTf.getExtensions(true).size()).isEqualTo(1);
        assertThat(createdTf.getExtensions(true)
                .findOrAddByType(SimpleComment.class).getValue())
                .isEqualTo("This is one comment");
    }

    @Test
    @RunAsClient
    public void putJsonResource() throws Exception {
        // Create a new Resource
        final Resource res = new Resource("new-put-resource");
        res.setType(ResourceType.FILE);
        res.setContentType(ContentType.TextPlain);
        res.setLang(LocaleId.EN_US);
        res.setRevision(1);
        res.getExtensions(true).add(new PoHeader("This is a PO Header"));

        TextFlow tf1 = new TextFlow("tf1", LocaleId.EN_US, "First Text Flow");
        tf1.getExtensions(true).add(new SimpleComment("This is one comment"));
        res.getTextFlows().add(tf1);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r/"
                        + res.getName()), "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {

                return webTarget.queryParam("ext", SimpleComment.ID).queryParam(
                        "ext", PoHeader.ID).request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(res), MediaType.APPLICATION_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // 201
                assertThat(response.getStatus())
                        .isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();

        SourceDocResource translationsClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        // Verify that it was created successfully
        Response resourceResponse =
                translationsClient.getResource(res.getName(), new StringSet(
                        PoHeader.ID + ";" + SimpleComment.ID));
        Resource createdResource = getResourceFromResponse(resourceResponse);

        assertThat(createdResource.getName()).isEqualTo(res.getName());
        assertThat(createdResource.getType()).isEqualTo(res.getType());
        assertThat(createdResource.getContentType()).isEqualTo(res.getContentType());
        assertThat(createdResource.getLang()).isEqualTo(res.getLang());
        // Created, so revision 1
        assertThat(createdResource.getRevision()).isEqualTo(1);

        // Extensions
        assertThat(createdResource.getExtensions(true).size()).isGreaterThanOrEqualTo(1);
        assertThat(
                createdResource.getExtensions(true).findByType(PoHeader.class)
                        .getComment()).isEqualTo("This is a PO Header");

        // Text Flow
        assertThat(createdResource.getTextFlows().size()).isEqualTo(1);

        TextFlow createdTf = createdResource.getTextFlows().get(0);
        assertThat(createdTf.getContents()).isEqualTo(tf1.getContents());
        assertThat(createdTf.getId()).isEqualTo(tf1.getId());
        assertThat(createdTf.getLang()).isEqualTo(tf1.getLang());
        // Create, so revision 1
        assertThat(createdTf.getRevision()).isEqualTo(1);

        // Text Flow extensions
        assertThat(createdTf.getExtensions(true).size()).isEqualTo(1);
        assertThat(createdTf.getExtensions(true)
                .findOrAddByType(SimpleComment.class).getValue())
                .isEqualTo("This is one comment");
    }

    @Test
    @RunAsClient
    public void getJsonResourceMeta() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", SimpleComment.ID).request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // 200
                assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, ResourceMeta.class);

                ResourceMeta resMeta =
                        jsonUnmarshal(entityString, ResourceMeta.class);
                assertThat(resMeta.getName()).isEqualTo("my/path/document-2.txt");
                assertThat(resMeta.getType()).isEqualTo(ResourceType.FILE);
                assertThat(resMeta.getLang()).isEqualTo(LocaleId.EN_US);
                assertThat(resMeta.getContentType()).isEqualTo(ContentType.TextPlain);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putJsonResourceMeta() throws Exception {
        SourceDocResource translationsClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        final ResourceMeta resMeta = new ResourceMeta();
        resMeta.setName("my/path/document-2.txt");
        resMeta.setType(ResourceType.FILE);
        resMeta.setContentType(ContentType.TextPlain);
        resMeta.setLang(LocaleId.EN_US);
        resMeta.setRevision(1);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", SimpleComment.ID).request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(resMeta),
                                MediaType.APPLICATION_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // 200
                assertThat(response.getStatus())
                        .isEqualTo(Status.OK.getStatusCode());
            }
        }.run();

        // Fetch again
        Response getResponse =
                translationsClient.getResourceMeta("my,path,document-2.txt",
                        null);
        ResourceMeta newResMeta = getResourceMetaFromResponse(getResponse);

        // 200
        assertThat(getResponse.getStatus()).isEqualTo(Status.OK.getStatusCode());
        assertThat(newResMeta.getName()).isEqualTo(resMeta.getName());
        assertThat(newResMeta.getContentType()).isEqualTo(resMeta.getContentType());
        assertThat(newResMeta.getLang()).isEqualTo(resMeta.getLang());
        assertThat(newResMeta.getType()).isEqualTo(resMeta.getType());
        // Updated so higher revision
        assertThat(newResMeta.getRevision()).isGreaterThan(1);
    }

    @Test
    @RunAsClient
    public void getJsonTranslations() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt/translations/"
                        + LocaleId.EN_US), "GET", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", SimpleComment.ID).request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // 200
                assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, TranslationsResource.class);

                TranslationsResource transRes =
                        jsonUnmarshal(entityString, TranslationsResource.class);
                assertThat(transRes.getTextFlowTargets().size()).isGreaterThanOrEqualTo(3);

                // First Text Flow Target
                TextFlowTarget tft1 = transRes.getTextFlowTargets().get(0);
                assertThat(tft1.getResId()).isEqualTo("tf2");
                assertThat(tft1.getState()).isEqualTo(ContentState.NeedReview);
                assertThat(tft1.getContents().get(0)).isEqualTo("mssgTrans1");
                assertThat(
                        tft1.getExtensions(true)
                                .findByType(SimpleComment.class).getValue()).isEqualTo("Text Flow Target Comment 1");
                assertThat(tft1.getTranslator().getName()).isEqualTo("Sample User");
                assertThat(tft1.getTranslator().getEmail()).isEqualTo("user1@localhost");

                // Second Text Flow Target
                TextFlowTarget tft2 = transRes.getTextFlowTargets().get(1);
                assertThat(tft2.getResId()).isEqualTo("tf3");
                assertThat(tft2.getState()).isEqualTo(ContentState.NeedReview);
                assertThat(tft2.getContents().get(0)).isEqualTo("mssgTrans2");
                assertThat(
                        tft2.getExtensions(true).findByType(SimpleComment.class)
                                .getValue())
                        .isEqualTo("Text Flow Target Comment 2");
                assertThat(tft2.getTranslator().getName())
                        .isEqualTo("Sample User");
                assertThat(tft2.getTranslator().getEmail())
                        .isEqualTo("user1@localhost");

                // First Text Flow Target
                TextFlowTarget tft3 = transRes.getTextFlowTargets().get(2);
                assertThat(tft3.getResId()).isEqualTo("tf4");
                assertThat(tft3.getState()).isEqualTo(ContentState.NeedReview);
                assertThat(tft3.getContents().get(0)).isEqualTo("mssgTrans3");
                assertThat(
                        tft3.getExtensions(true)
                                .findByType(SimpleComment.class).getValue())
                        .isEqualTo("Text Flow Target Comment 3");
                assertThat(tft3.getTranslator().getName())
                        .isEqualTo("Sample User");
                assertThat(tft3.getTranslator().getEmail())
                        .isEqualTo("user1@localhost");
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putJsonTranslations() throws Exception {
        // Get the original translations
        TranslatedDocResource translationsClient = getTransResource("/projects/p/sample-project/iterations/i/1.0/r/",
                AuthenticatedAsUser.Admin);
        Response response =
                translationsClient.getTranslations("my,path,document-2.txt",
                        LocaleId.EN_US, new StringSet(SimpleComment.ID), false,
                        null);
        final TranslationsResource transRes = getTransResourceFromResponse(response);

        // 200
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        assertThat(transRes.getTextFlowTargets().size()).isGreaterThanOrEqualTo(3);

        // Alter the translations
        transRes.getTextFlowTargets().get(0).setContents("Translated 1");
        transRes.getTextFlowTargets().get(1).setContents("Translated 2");
        transRes.getTextFlowTargets().get(2).setContents("Translated 3");

        transRes.getTextFlowTargets().get(0).setState(ContentState.Approved);
        transRes.getTextFlowTargets().get(1).setState(ContentState.Approved);
        transRes.getTextFlowTargets().get(2).setState(ContentState.Approved);

        transRes.getTextFlowTargets().get(0).getExtensions(true)
                .add(new SimpleComment("Translated Comment 1"));
        transRes.getTextFlowTargets().get(1).getExtensions(true)
                .add(new SimpleComment("Translated Comment 2"));
        transRes.getTextFlowTargets().get(2).getExtensions(true)
                .add(new SimpleComment("Translated Comment 3"));

        // Put the translations
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt/translations/"
                        + LocaleId.EN_US), "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", SimpleComment.ID).request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(transRes),
                                MediaType.APPLICATION_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // 200
                assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
            }
        }.run();

        // Retrieve the translations once more to make sure they were changed
        // accordingly
        response =
                translationsClient.getTranslations("my,path,document-2.txt",
                        LocaleId.EN_US, new StringSet(SimpleComment.ID), false, null);
        TranslationsResource updatedTransRes = getTransResourceFromResponse(response);

        // 200
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        assertThat(updatedTransRes.getTextFlowTargets().size()).isGreaterThanOrEqualTo(3);

        // First Text Flow Target
        TextFlowTarget tft1 = updatedTransRes.getTextFlowTargets().get(0);
        assertThat(tft1.getResId()).isEqualTo("tf2");
        assertThat(tft1.getState()).isEqualTo(ContentState.Approved);
        assertThat(tft1.getContents().get(0)).isEqualTo("Translated 1");
        assertThat(tft1.getExtensions(true).findByType(SimpleComment.class)
                .getValue()).isEqualTo("Translated Comment 1");

        // Second Text Flow Target
        TextFlowTarget tft2 = updatedTransRes.getTextFlowTargets().get(1);
        assertThat(tft2.getResId()).isEqualTo("tf3");
        assertThat(tft2.getState()).isEqualTo(ContentState.Approved);
        assertThat(tft2.getContents().get(0)).isEqualTo("Translated 2");
        assertThat(tft2.getExtensions(true).findByType(SimpleComment.class)
                .getValue()).isEqualTo("Translated Comment 2");

        // First Text Flow Target
        TextFlowTarget tft3 = updatedTransRes.getTextFlowTargets().get(2);
        assertThat(tft3.getResId()).isEqualTo("tf4");
        assertThat(tft3.getState()).isEqualTo(ContentState.Approved);
        assertThat(tft3.getContents().get(0)).isEqualTo("Translated 3");
        assertThat(tft3.getExtensions(true).findByType(SimpleComment.class)
                .getValue()).isEqualTo("Translated Comment 3");
    }

}
