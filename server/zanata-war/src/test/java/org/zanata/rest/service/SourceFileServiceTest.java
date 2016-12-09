/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.MockResourceFactory;
import org.zanata.ZanataTest;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.DocumentPersistService;
import org.zanata.file.DocumentPersistService.PersistedDocumentInfo;
import org.zanata.file.DocumentUploadUtil;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.zanata.util.JavaslangNext.TODO;

/**
 * @see TMXDummyRestTest
 * @see org.zanata.ZanataRestTest
 */
@RunWith(CdiUnitRunner.class)
public class SourceFileServiceTest extends ZanataTest {

    private String projectSlug = "proj";
    private String versionSlug = "ver";
    // note that this doesn't start with "/rest" (as it would at runtime)
    private String servicePath = RestConstants.SOURCE_FILE_SERVICE_PATH
                .replace("{projectSlug}", projectSlug)
                .replace("{versionSlug}", versionSlug);
    private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

    @Produces
    @Mock
    private ProjectIterationDAO projectIterationDAO;
//    @Produces
//    @Mock(answer = RETURNS_SMART_NULLS)
//    private DocumentDAO documentDAO;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private LegacyFileMapper legacyFileMapper;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private ProjectUtil projectUtil;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private ZanataIdentity identity;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private JobStatusService jobStatusService;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private SourceDocumentUpload sourceUploader;
    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private DocumentUploadUtil uploadUtil;
    @Produces
    @Mock(answer = RETURNS_DEFAULTS)
    private DocumentPersistService documentPersistService;

//    @Produces
//    @Mock(answer = RETURNS_SMART_NULLS)
//    private FilePersistService filePersistService;

    @Inject
    private SourceFileService sourceFileService;

    @Before
    public void setup() {
        dispatcher.getRegistry().addResourceFactory(new MockResourceFactory(sourceFileService));
    }

    @Test
    @InRequestScope
    public void getNonexistentDocReturns404() throws Exception {
        String docId = "nonexistentDoc";
        GlobalDocumentId globalId = new GlobalDocumentId(projectSlug, versionSlug, docId);
        when(documentPersistService.getSourceDocumentForStreaming(globalId)).thenReturn(null);

        String path = servicePath + "?docId=" + docId + "&projectType=GETTEXT";
        MockHttpRequest request = MockHttpRequest.get(path);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    @InRequestScope
    public void getRawDocReturnsContents() throws Exception {
        String docId = "rawFileDoc";
        GlobalDocumentId globalId = new GlobalDocumentId(projectSlug, versionSlug, docId);

//        HDocument document = new HDocument();
//        HRawDocument rawDocument = new HRawDocument();
//        document.setRawDocument(rawDocument);
//        when(documentDAO.getByGlobalId(globalId)).thenReturn(document);

        byte[] dummyDoc = "This is a dummy document".getBytes(UTF_8);
        PersistedDocumentInfo docInfo = new PersistedDocumentInfo(docId,
                (long) dummyDoc.length, new ByteArrayInputStream(dummyDoc));
        when(documentPersistService.getSourceDocumentForStreaming(globalId)).thenReturn(docInfo);

        String path = servicePath + "?docId=" + docId + "&projectType=GETTEXT";
        MockHttpRequest request = MockHttpRequest.get(path);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(200);
        byte[] responseContent = response.getOutput();
        assertThat(responseContent).containsExactly(dummyDoc);
    }

    @Test
    @InRequestScope
    @Ignore("incomplete feature ZNTA-1302")
    public void getGettextDocReturnsContents() throws Exception {
        String docId = "gettextDoc";
//        HDocument document = new HDocument();
        GlobalDocumentId globalId = new GlobalDocumentId(projectSlug, versionSlug, docId);
//        when(documentDAO.getByGlobalId(globalId)).thenReturn(document);
//        byte[] dummyDoc = "This is a dummy document".getBytes(UTF_8);
//        PersistedDocumentInfo docInfo = new PersistedDocumentInfo(docId, dummyDoc.length, new ByteArrayInputStream(dummyDoc));
//        when(documentPersistService.getRawDocumentForStreaming(globalId)).thenReturn(docInfo);

        String path = servicePath + "?docId=" + docId + "&projectType=GETTEXT";
        MockHttpRequest request = MockHttpRequest.get(path);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(200);
        String responseContent = response.getContentAsString();
        String expectedPotContent = TODO();
        assertThat(responseContent).isEqualTo(expectedPotContent);
    }

    @Test
    @InRequestScope
    @Ignore("incomplete feature ZNTA-1302")
    public void getPropertiesDocReturnsContents() throws Exception {
        TODO();
    }

    @Test
    @InRequestScope
    @Ignore("incomplete feature ZNTA-1302")
    public void putDocReturns204() throws Exception {
        byte[] content = "my document".getBytes(UTF_8);
        String path = servicePath +
                "?docId=newDoc&projectType=GETTEXT&lang=en&size=" + content.length;
        MockHttpRequest request = MockHttpRequest.put(path);
        request.content(content);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(204);
//        String responseContent = response.getContentAsString();
//        assertThat(responseContent).isEqualTo("TODO");
        TODO();
    }

}
