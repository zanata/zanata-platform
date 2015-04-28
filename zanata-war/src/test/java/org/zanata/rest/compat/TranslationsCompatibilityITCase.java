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

import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
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
import org.zanata.apicompat.rest.service.SourceDocResource;
import org.zanata.apicompat.rest.service.TranslatedDocResource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;

public class TranslationsCompatibilityITCase extends CompatibilityBase {

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
    public void postResource() throws Exception {
        // Create a new Resource
        Resource res = new Resource("new-resource");
        res.setType(ResourceType.FILE);
        res.setContentType(ContentType.TextPlain);
        res.setLang(LocaleId.EN_US);
        res.setRevision(1);
        res.getExtensions(true).add(new PoHeader("This is a PO Header"));

        TextFlow tf1 = new TextFlow("tf1", LocaleId.EN_US, "First Text Flow");
        tf1.getExtensions(true).add(new SimpleComment("This is one comment"));
        res.getTextFlows().add(tf1);

        SourceDocResource sourceDocClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        Response response =
                sourceDocClient.post(res, new StringSet(PoHeader.ID + ";"
                        + SimpleComment.ID), true);

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode())); // 201
        releaseConnection(response);

        // Verify that it was created successfully
        Response resourceResponse =
                sourceDocClient.getResource(res.getName(), new StringSet(
                        PoHeader.ID + ";" + SimpleComment.ID));
        Resource createdResource = getResourceFromResponse(resourceResponse);

        assertThat(createdResource.getName(), is(res.getName()));
        assertThat(createdResource.getType(), is(res.getType()));
        assertThat(createdResource.getContentType(), is(res.getContentType()));
        assertThat(createdResource.getLang(), is(res.getLang()));
        assertThat(createdResource.getRevision(), is(1)); // Created, so
                                                          // revision 1

        // Extensions
        assertThat(createdResource.getExtensions(true).size(),
                greaterThanOrEqualTo(1));
        assertThat(
                createdResource.getExtensions(true).findByType(PoHeader.class)
                        .getComment(), is("This is a PO Header"));

        // Text Flow
        assertThat(createdResource.getTextFlows().size(), is(1));

        TextFlow createdTf = createdResource.getTextFlows().get(0);
        assertThat(createdTf.getContents().get(0), is(tf1.getContents().get(0)));
        assertThat(createdTf.getId(), is(tf1.getId()));
        assertThat(createdTf.getLang(), is(tf1.getLang()));
        assertThat(createdTf.getRevision(), is(1)); // Create, so revision 1

        // Text Flow extensions
        assertThat(createdTf.getExtensions(true).size(), is(1));
        assertThat(
                createdTf.getExtensions(true)
                        .findOrAddByType(SimpleComment.class).getValue(),
                is("This is one comment"));
    }

    @Test
    @RunAsClient
    public void doublePostResource() throws Exception {
        // Create a new Resource
        Resource res = new Resource("double-posted-resource");
        res.setType(ResourceType.FILE);
        res.setContentType(ContentType.TextPlain);
        res.setLang(LocaleId.EN_US);
        res.setRevision(1);

        // Post once
        SourceDocResource sourceDocClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        Response response =
                sourceDocClient.post(res, new StringSet(PoHeader.ID + ";"
                        + SimpleComment.ID), true);

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode())); // 201
        releaseConnection(response);

        // Post Twice (should conflict)
        response =
                sourceDocClient.post(res, new StringSet(PoHeader.ID + ";"
                        + SimpleComment.ID), true);

        assertThat(response.getStatus(), is(Status.CONFLICT.getStatusCode())); // 409
        releaseConnection(response);
    }

    @Test
    @RunAsClient
    public void putResource() throws Exception {
        // Create a new Resource
        Resource res = new Resource("new-put-resource");
        res.setType(ResourceType.FILE);
        res.setContentType(ContentType.TextPlain);
        res.setLang(LocaleId.EN_US);
        res.setRevision(1);
        res.getExtensions(true).add(new PoHeader("This is a PO Header"));

        TextFlow tf1 = new TextFlow("tf1", LocaleId.EN_US, "First Text Flow");
        tf1.getExtensions(true).add(new SimpleComment("This is one comment"));
        res.getTextFlows().add(tf1);

        SourceDocResource sourceDocClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        Response response =
                sourceDocClient.putResource(res.getName(), res, new StringSet(
                        PoHeader.ID + ";" + SimpleComment.ID), false);

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode())); // 201
        releaseConnection(response);

        // Verify that it was created successfully
        Response resourceResponse =
                sourceDocClient.getResource(res.getName(), new StringSet(
                        PoHeader.ID + ";" + SimpleComment.ID));
        Resource createdResource = getResourceFromResponse(resourceResponse);

        assertThat(createdResource.getName(), is(res.getName()));
        assertThat(createdResource.getType(), is(res.getType()));
        assertThat(createdResource.getContentType(), is(res.getContentType()));
        assertThat(createdResource.getLang(), is(res.getLang()));
        assertThat(createdResource.getRevision(), is(1)); // Created, so
                                                          // revision 1

        // Extensions
        assertThat(createdResource.getExtensions(true).size(),
                greaterThanOrEqualTo(1));
        assertThat(
                createdResource.getExtensions(true).findByType(PoHeader.class)
                        .getComment(), is("This is a PO Header"));

        // Text Flow
        assertThat(createdResource.getTextFlows().size(), is(1));

        TextFlow createdTf = createdResource.getTextFlows().get(0);
        assertThat(createdTf.getContents().get(0), is(tf1.getContents().get(0)));
        assertThat(createdTf.getId(), is(tf1.getId()));
        assertThat(createdTf.getLang(), is(tf1.getLang()));
        assertThat(createdTf.getRevision(), is(1)); // Create, so revision 1

        // Text Flow extensions
        assertThat(createdTf.getExtensions(true).size(), is(1));
        assertThat(
                createdTf.getExtensions(true)
                        .findOrAddByType(SimpleComment.class).getValue(),
                is("This is one comment"));
    }

    @Test
    @RunAsClient
    public void getXmlResource() throws Exception {
        SourceDocResource sourceDocClient = getSourceDocResource("/projects/p/sample-project/iterations/i/1.0/r/");
        Response response =
                sourceDocClient.getResource("my,path,document-2.txt",
                        new StringSet(SimpleComment.ID));
        Resource resource = getResourceFromResponse(response);

        assertThat(resource.getName(), is("my/path/document-2.txt"));
        assertThat(resource.getType(), is(ResourceType.FILE));
        assertThat(resource.getLang(), is(LocaleId.EN_US));

        // Make sure all Text Flows are present
        assertThat(resource.getTextFlows().size(), greaterThanOrEqualTo(3));

        // Evaluate individual text flows
        TextFlow txtFlow = resource.getTextFlows().get(0);
        assertThat(txtFlow.getId(), is("tf2"));
        assertThat(txtFlow.getRevision(), is(1));
        assertThat(txtFlow.getContents().get(0), is("mssgId1"));

        txtFlow = resource.getTextFlows().get(1);
        assertThat(txtFlow.getId(), is("tf3"));
        assertThat(txtFlow.getRevision(), is(1));
        assertThat(txtFlow.getContents().get(0), is("mssgId2"));

        txtFlow = resource.getTextFlows().get(2);
        assertThat(txtFlow.getId(), is("tf4"));
        assertThat(txtFlow.getRevision(), is(1));
        assertThat(txtFlow.getContents().get(0), is("mssgId3"));
    }

    @Test
    @RunAsClient
    public void deleteResource() throws Exception {
        // Create a new Resource
        Resource res = new Resource("delete-resource");
        res.setType(ResourceType.FILE);
        res.setContentType(ContentType.TextPlain);
        res.setLang(LocaleId.EN_US);
        res.setRevision(1);

        SourceDocResource sourceDocClient = getSourceDocResource(
                "/projects/p/sample-project/iterations/i/1.0/r/");

        Response response =
                sourceDocClient.putResource(res.getName(), res, new StringSet(
                        PoHeader.ID + ";" + SimpleComment.ID), false);

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode())); // 201
        releaseConnection(response);

        // Delete the resource
        Response deleteResponse =
                sourceDocClient.deleteResource(res.getName());

        assertThat(deleteResponse.getStatus(), is(Status.OK.getStatusCode())); // 200
        releaseConnection(response);

        // try to fetch it again
        Response getResponse =
                sourceDocClient.getResource(res.getName(), null);
        assertThat(getResponse.getStatus(),
                is(Status.NOT_FOUND.getStatusCode())); // 404
        releaseConnection(getResponse);
    }

    @Test
    @RunAsClient
    public void getResourceMeta() throws Exception {
        SourceDocResource sourceDocClient = getSourceDocResource(
                "/projects/p/sample-project/iterations/i/1.0/r/");
        Response response =
                sourceDocClient.getResourceMeta("my,path,document-2.txt",
                        new StringSet(SimpleComment.ID));
        ResourceMeta resMeta = getResourceMetaFromResponse(response);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
        assertThat(resMeta.getName(), is("my/path/document-2.txt"));
        assertThat(resMeta.getType(), is(ResourceType.FILE));
        assertThat(resMeta.getLang(), is(LocaleId.EN_US));
        assertThat(resMeta.getContentType(), is(ContentType.TextPlain));
    }

    @Test
    @RunAsClient
    public void putResourceMeta() throws Exception {
        SourceDocResource sourceDocClient = getSourceDocResource(
                "/projects/p/sample-project/iterations/i/1.0/r/");
        ResourceMeta resMeta = new ResourceMeta();
        resMeta.setName("my/path/document-2.txt");
        resMeta.setType(ResourceType.FILE);
        resMeta.setContentType(ContentType.TextPlain);
        resMeta.setLang(LocaleId.EN_US);
        resMeta.setRevision(1);

        Response putResponse =
                sourceDocClient.putResourceMeta("my,path,document-2.txt",
                        resMeta, null);
        assertThat(putResponse.getStatus(), is(Status.OK.getStatusCode())); // 200
        releaseConnection(putResponse);

        // Fetch again
        Response getResponse =
                sourceDocClient.getResourceMeta("my,path,document-2.txt", null);
        ResourceMeta newResMeta = getResourceMetaFromResponse(getResponse);

        assertThat(getResponse.getStatus(), is(Status.OK.getStatusCode())); // 200
        assertThat(newResMeta.getName(), is(resMeta.getName()));
        assertThat(newResMeta.getContentType(), is(resMeta.getContentType()));
        assertThat(newResMeta.getLang(), is(resMeta.getLang()));
        assertThat(newResMeta.getType(), is(resMeta.getType()));
        assertThat(newResMeta.getRevision(), is(1)); // Created, so revision 1
    }

    @Test
    @RunAsClient
    public void getTranslations() throws Exception {
        TranslatedDocResource translationsClient = getTransResource(
                "/projects/p/sample-project/iterations/i/1.0/r/",
                AuthenticatedAsUser.Admin);
        Response response =
                translationsClient.getTranslations("my,path,document-2.txt",
                        LocaleId.EN_US, new StringSet(SimpleComment.ID), false,
                        null);
        TranslationsResource transRes = getTransResourceFromResponse(response);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
        assertThat(transRes.getTextFlowTargets().size(),
                greaterThanOrEqualTo(3));

        // First Text Flow Target
        TextFlowTarget tft1 = transRes.getTextFlowTargets().get(0);
        assertThat(tft1.getResId(), is("tf2"));
        assertThat(tft1.getState(), is(ContentState.NeedReview));
        assertThat(tft1.getContents().get(0), is("mssgTrans1"));
        assertThat(tft1.getExtensions(true).findByType(SimpleComment.class)
                .getValue(), is("Text Flow Target Comment 1"));
        assertThat(tft1.getTranslator().getName(), is("Sample User"));
        assertThat(tft1.getTranslator().getEmail(), is("user1@localhost"));

        // Second Text Flow Target
        TextFlowTarget tft2 = transRes.getTextFlowTargets().get(1);
        assertThat(tft2.getResId(), is("tf3"));
        assertThat(tft2.getState(), is(ContentState.NeedReview));
        assertThat(tft2.getContents().get(0), is("mssgTrans2"));
        assertThat(tft2.getExtensions(true).findByType(SimpleComment.class)
                .getValue(), is("Text Flow Target Comment 2"));
        assertThat(tft2.getTranslator().getName(), is("Sample User"));
        assertThat(tft2.getTranslator().getEmail(), is("user1@localhost"));

        // First Text Flow Target
        TextFlowTarget tft3 = transRes.getTextFlowTargets().get(2);
        assertThat(tft3.getResId(), is("tf4"));
        assertThat(tft3.getState(), is(ContentState.NeedReview));
        assertThat(tft3.getContents().get(0), is("mssgTrans3"));
        assertThat(tft3.getExtensions(true).findByType(SimpleComment.class)
                .getValue(), is("Text Flow Target Comment 3"));
        assertThat(tft3.getTranslator().getName(), is("Sample User"));
        assertThat(tft3.getTranslator().getEmail(), is("user1@localhost"));
    }

    @Test
    @RunAsClient
    public void putTranslations() throws Exception {
        TranslatedDocResource translationsClient = getTransResource("/projects/p/sample-project/iterations/i/1.0/r/",
                AuthenticatedAsUser.Admin);
        Response response =
                translationsClient.getTranslations("my,path,document-2.txt",
                        LocaleId.EN_US, new StringSet(SimpleComment.ID), false,
                        null);
        TranslationsResource transRes = getTransResourceFromResponse(response);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
        assertThat(transRes.getTextFlowTargets().size(),
                greaterThanOrEqualTo(3));

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
        Response putResponse =
                translationsClient.putTranslations("my,path,document-2.txt",
                        LocaleId.EN_US, transRes, new StringSet(
                                SimpleComment.ID), "auto");

        assertThat(putResponse.getStatus(), is(Status.OK.getStatusCode())); // 200
        releaseConnection(putResponse);

        // Retrieve the translations once more to make sure they were changed
        // accordingly
        response =
                translationsClient.getTranslations("my,path,document-2.txt",
                        LocaleId.EN_US, new StringSet(SimpleComment.ID), false, null);
        transRes = getTransResourceFromResponse(response);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
        assertThat(transRes.getTextFlowTargets().size(),
                greaterThanOrEqualTo(3));

        // First Text Flow Target
        TextFlowTarget tft1 = transRes.getTextFlowTargets().get(0);
        assertThat(tft1.getResId(), is("tf2"));
        assertThat(tft1.getState(), is(ContentState.Approved));
        assertThat(tft1.getContents().get(0), is("Translated 1"));
        assertThat(tft1.getExtensions(true).findByType(SimpleComment.class)
                .getValue(), is("Translated Comment 1"));


        // Second Text Flow Target
        TextFlowTarget tft2 = transRes.getTextFlowTargets().get(1);
        assertThat(tft2.getResId(), is("tf3"));
        assertThat(tft2.getState(), is(ContentState.Approved));
        assertThat(tft2.getContents().get(0), is("Translated 2"));
        assertThat(tft2.getExtensions(true).findByType(SimpleComment.class)
                .getValue(), is("Translated Comment 2"));

        // First Text Flow Target
        TextFlowTarget tft3 = transRes.getTextFlowTargets().get(2);
        assertThat(tft3.getResId(), is("tf4"));
        assertThat(tft3.getState(), is(ContentState.Approved));
        assertThat(tft3.getContents().get(0), is("Translated 3"));
        assertThat(tft3.getExtensions(true).findByType(SimpleComment.class)
                .getValue(), is("Translated Comment 3"));
    }

    @Test
    @RunAsClient
    public void deleteTranslations() throws Exception {
        TranslatedDocResource translationsClient = getTransResource("/projects/p/sample-project/iterations/i/1.0/r/",
                AuthenticatedAsUser.Admin);
        Response response =
                translationsClient.deleteTranslations("my,path,document-3.txt",
                        LocaleId.EN_US);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
        releaseConnection(response);

        // try to fetch them again
        Response getResponse =
                translationsClient.getTranslations("my,path,document-3.txt",
                        LocaleId.EN_US, new StringSet(PoHeader.ID + ";"
                                + SimpleComment.ID), false, null);
        List<TextFlowTarget> targets =
                getTransResourceFromResponse(getResponse).getTextFlowTargets();
        for (TextFlowTarget target : targets) {
            assertThat(target.getState(), is(ContentState.New));
        }
    }
}
