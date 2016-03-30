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

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@RunWith(CdiUnitRunner.class)
public class TranslationDocumentUploadTest extends DocumentUploadTest {

    private static final String ANY_LOCALE = "es";
    private static final String ANY_MERGETYPE = "auto";

    @Inject
    private TranslationDocumentUpload transUpload;

    @Produces @Mock TranslationService translationService;
    @Produces @Mock TranslationFileService translationFileService;
    @Produces @Mock UploadPartPersistService uploadPartPersistService;
    @Produces @Mock WindowContext windowContext;
    @Produces @Mock UrlUtil urlUtil;
    @Produces @Mock Session session;
    @Produces @SessionId String sessionId = "";
    @Produces @ServerPath String serverPath = "";
    @Produces @ContextPath String contextPath = "";

    @After
    public void clearResponse() {
        response = null;
    }

    @Test
    public void failsIfNotLoggedIn() {
        conf = defaultUpload().build();
        response =
                transUpload.tryUploadTranslationFile(conf.id, ANY_LOCALE,
                        ANY_MERGETYPE, false, conf.uploadForm, TranslationSourceType.API_UPLOAD);
        assertResponseHasStatus(Response.Status.UNAUTHORIZED);
        assertUploadTerminated();
    }

    // TODO damason: test failure when document does not exist
    // TODO damason: test failure if type is not po or adapter type
    // TODO damason: test failure if lacking translation upload permission
    // TODO damason: test basic translation upload successful

}
