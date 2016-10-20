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

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.ZanataTest;
import org.zanata.common.ProjectType;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.i18n.Messages;
import org.zanata.jpa.FullText;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.service.CopyTransService;
import org.zanata.service.FileSystemService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class FileServiceTest extends ZanataTest {
    private static final String PROJ_SLUG = "project-slug";
    private static final String VER_SLUG = "version-slug";
    private static final String DOC_ID = "docId";
    private static final String LOCALE = "es";
    private static final String MERGE = "auto";

    @Produces @Mock
    private SourceDocumentUpload sourceUploader;
    @Produces @Mock
    private TranslationDocumentUpload transUploader;

    // Mocked injected items
    @Produces @ContextPath String contextPath = "/mock-context";
    @Produces @SessionId String sessionId = "mock-session-id";
    @Produces @ServerPath String serverPath = "mock-server-path";
    @Produces @Mock Session session;
    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;
    @Produces @Mock EntityManager entityManager;
    @Produces @Mock LocaleService localeService;
    @Produces @Mock CopyTransService copyTransService;
    @Produces @Mock FilePersistService filePersistService;
    @Produces @Mock FileSystemService fileSystemService;
    @Produces @Mock TranslationFileService translationFileService;
    @Produces @Mock TranslationService translationService;
    @Produces @Mock WindowContext windowContext;
    @Produces @Mock UrlUtil urlUtil;
    // needed to override the producers of the original class
    @Produces @Mock ApplicationConfiguration applicationConfiguration;
    @Produces @Mock Messages messages;
    @Produces @Mock LegacyFileMapper legacyFileMapper;
//    @Inject LegacyFileMapper legacyFileMapper;

    @Captor
    private ArgumentCaptor<DocumentFileUploadForm> formCaptor;

    @Inject
    private SourceFileService sourceFileService;
    @Inject
    private TranslatedFileResourceService translationFileResource;

    private GlobalDocumentId id;
    private DocumentFileUploadForm form;
    private Response okResponse;
    private Response response;

    @Before
    public void beforeTest() {
        id = new GlobalDocumentId(PROJ_SLUG, VER_SLUG, DOC_ID);
        form = new DocumentFileUploadForm();
        okResponse = Response.ok().build();
        // just return unaltered docId
        when(legacyFileMapper.getFilenameSuffix(any(), any(), any(), anyBoolean())).thenReturn("");
//        when(legacyFileMapper.getServerDocId(any(), any(), any(), any())).thenAnswer(invoc -> invoc.getArguments()[2]);
    }

    @After
    public void afterMethod() {
        id = null;
        form = null;
        okResponse = null;
        response = null;
    }

    @Test
    @InRequestScope
    public void sourceUploadParamsHandledCorrectly() {
        when(sourceUploader.tryUploadSourceFile(eq(id), formCaptor.capture()))
                .thenReturn(okResponse);
        // FIXME
//        sourceFileService.uploadSourceFile(PROJ_SLUG, VER_SLUG, DOC_ID, form,
//                ProjectType.File);
//        assertThat(formCaptor.getValue(), is(sameInstance(form)));
    }

    @Test
    @InRequestScope
    public void sourceUploadResponseReturnedDirectly() {
        when(sourceUploader.tryUploadSourceFile(id, form)).thenReturn(
                okResponse);
        // FIXME
//        response =
//                sourceFileService.uploadSourceFile(PROJ_SLUG, VER_SLUG, DOC_ID, form, ProjectType.File);
//        assertThat(response, is(sameInstance(okResponse)));
    }

    @Test
    @InRequestScope
    public void translationUploadParamsHandledCorrectly() {
        when(
                transUploader.tryUploadTranslationFile(eq(id), eq(LOCALE),
                        eq(MERGE), eq(false), formCaptor.capture(), eq(TranslationSourceType.API_UPLOAD)))
                .thenReturn(okResponse);
        // FIXME
//        translationFileResource.uploadTranslationFile(PROJ_SLUG, VER_SLUG, LOCALE, DOC_ID,
//                MERGE, form, null);
//        assertThat(formCaptor.getValue(), is(sameInstance(form)));
    }

    @Test
    @InRequestScope
    public void translationUploadResponseReturnedDirectly() {
        when(transUploader.tryUploadTranslationFile(id, LOCALE, MERGE, false, form, TranslationSourceType.API_UPLOAD))
                .thenReturn(okResponse);
        // FIXME
//        response =
//                translationFileResource.uploadTranslationFile(PROJ_SLUG, VER_SLUG, LOCALE,
//                        DOC_ID, MERGE, form, null);
//        assertThat(response, is(sameInstance(okResponse)));
    }
}
