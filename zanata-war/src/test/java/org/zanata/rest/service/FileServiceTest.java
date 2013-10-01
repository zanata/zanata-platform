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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.seam.SeamAutowire;

/**
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class FileServiceTest {
    private static final String PROJ_SLUG = "project-slug";
    private static final String VER_SLUG = "version-slug";
    private static final String DOC_ID = "docId";
    private static final String LOCALE = "es";
    private static final String MERGE = "auto";

    SeamAutowire seam = SeamAutowire.instance();

    @Mock
    private SourceDocumentUpload sourceUploader;
    @Mock
    private TranslationDocumentUpload transUploader;

    @Captor
    private ArgumentCaptor<DocumentFileUploadForm> formCaptor;

    private FileResource fileService;

    private GlobalDocumentId id;
    private DocumentFileUploadForm form;
    private Response okResponse;
    private Response response;

    @BeforeMethod
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);

        seam.reset();
        seam.ignoreNonResolvable()
                .use("sourceDocumentUploader", sourceUploader)
                .use("translationDocumentUploader", transUploader)
                .allowCycles();

        fileService = seam.autowire(FileService.class);

        id = new GlobalDocumentId(PROJ_SLUG, VER_SLUG, DOC_ID);
        form = new DocumentFileUploadForm();
        okResponse = Response.ok().build();
    }

    @AfterMethod
    public void afterMethod() {
        id = null;
        form = null;
        okResponse = null;
        response = null;
    }

    public void sourceUploadParamsHandledCorrectly() {
        when(sourceUploader.tryUploadSourceFile(eq(id), formCaptor.capture()))
                .thenReturn(okResponse);
        fileService.uploadSourceFile(PROJ_SLUG, VER_SLUG, DOC_ID, form);
        assertThat(formCaptor.getValue(), is(sameInstance(form)));
    }

    public void sourceUploadResponseReturnedDirectly() {
        when(sourceUploader.tryUploadSourceFile(id, form)).thenReturn(
                okResponse);
        response =
                fileService.uploadSourceFile(PROJ_SLUG, VER_SLUG, DOC_ID, form);
        assertThat(response, is(sameInstance(okResponse)));
    }

    public void translationUploadParamsHandledCorrectly() {
        when(
                transUploader.tryUploadTranslationFile(eq(id), eq(LOCALE),
                        eq(MERGE), formCaptor.capture()))
                .thenReturn(okResponse);
        fileService.uploadTranslationFile(PROJ_SLUG, VER_SLUG, LOCALE, DOC_ID,
                MERGE, form);
        assertThat(formCaptor.getValue(), is(sameInstance(form)));
    }

    public void translationUploadResponseReturnedDirectly() {
        when(transUploader.tryUploadTranslationFile(id, LOCALE, MERGE, form))
                .thenReturn(okResponse);
        response =
                fileService.uploadTranslationFile(PROJ_SLUG, VER_SLUG, LOCALE,
                        DOC_ID, MERGE, form);
        assertThat(response, is(sameInstance(okResponse)));
    }
}
