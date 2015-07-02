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
package org.zanata.file;

import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static org.mockito.Mockito.doThrow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.type.TranslationSourceType;

public class TranslationDocumentUploadTest extends DocumentUploadTest {

    private static final String ANY_LOCALE = "es";
    private static final String ANY_MERGETYPE = "auto";

    @Mock
    DocumentUploadUtil documentUploadUtil;

    private TranslationDocumentUpload transUpload;

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
        seam.reset();
        seam.ignoreNonResolvable()
                .use("documentUploadUtil", documentUploadUtil).allowCycles();

        transUpload = seam.autowire(TranslationDocumentUpload.class);
    }

    @After
    public void clearResponse() {
        response = null;
    }

    @Test
    public void checksValidityAndFailsIfNotValid() {
        conf = defaultUpload().build();
        doThrow(new ChunkUploadException(NOT_ACCEPTABLE, "Test message")).when(
                documentUploadUtil).failIfUploadNotValid(conf.id,
                conf.uploadForm);
        response =
                transUpload.tryUploadTranslationFile(conf.id, ANY_LOCALE,
                        ANY_MERGETYPE, false, conf.uploadForm, TranslationSourceType.API_UPLOAD);
        assertResponseHasStatus(NOT_ACCEPTABLE);
        assertResponseHasErrorMessage("Test message");
        assertUploadTerminated();
    }

    // TODO damason: test failure when document does not exist
    // TODO damason: test failure if type is not po or adapter type
    // TODO damason: test failure if lacking translation upload permission
    // TODO damason: test basic translation upload successful

}
