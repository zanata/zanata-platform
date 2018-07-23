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
package org.zanata.service.impl;

import com.google.common.base.Optional;
import org.apache.commons.io.input.NullInputStream;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.HashUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
public class TranslationFileServiceImplTest extends ZanataTest {

    @Inject
    TranslationFileServiceImpl transFileService;

    @Mock
    @Produces
    private ProjectIterationDAO projectIterationDAO;

    @Mock
    @Produces
    private DocumentDAO documentDAO;

    @Produces @Mock Session session;

    @Test
    @InRequestScope
    public void hasPlainTextAdapter() {
        assertThat(transFileService.hasAdapterFor(DocumentType.PLAIN_TEXT)).isTrue();
    }

    @Test
    @InRequestScope
    public void parsePoInFileProject() throws Exception {
        String project = "test";
        String version = "master";
        String docId = "test.pot";
        String poName = "test.po";
        String poContent = "msgid \"\"\n" +
                "msgstr \"\"\n" +
                "\"Project-Id-Version: test-master\\n\"\n" +
                "\n" +
                "#, c-format\n" +
                "msgid \"Thing 1\"\n" +
                "msgid_plural \"%d files removed\"\n" +
                "msgstr[0] \"1 aoeuaouaou\"\n" +
                "msgstr[1] \"%d aoeuaouao\"\n" +
                "msgstr[2] \"\"";
        InputStream stream = new ByteArrayInputStream(
                poContent.getBytes(StandardCharsets.UTF_8));

        HProject hProject = new HProject();
        hProject.setDefaultProjectType(ProjectType.File);
        HProjectIteration hProjectIteration = new HProjectIteration();
        hProjectIteration.setProject(hProject);
        hProjectIteration.setProjectType(ProjectType.File);
        HDocument hDocument = new HDocument();

        when(projectIterationDAO.getBySlug(project, version))
                .thenReturn(hProjectIteration);
        when(documentDAO.getByDocIdAndIteration(hProjectIteration, docId))
                .thenReturn(hDocument);

        TranslationsResource translationsResource = transFileService
                .parseTranslationFile(stream, poName,
                "ru", project, version, docId, Optional.absent());

        TextFlowTarget target = translationsResource.getTextFlowTargets().get(0);
        assertThat(target.getContents().get(0)).isEqualTo("1 aoeuaouaou");
        assertThat(target.getResId()).isEqualTo(HashUtil.sourceHash("Thing 1"));
    }

    @Test
    @InRequestScope
    public void parseAdapterFileInFileProject() throws Exception {
        String project = "test";
        String version = "master";
        String docId = "test.properties";
        String plaintextContent = "first: test message";
        InputStream stream = new ByteArrayInputStream(
                plaintextContent.getBytes(StandardCharsets.UTF_8));

        HProject hProject = new HProject();
        hProject.setDefaultProjectType(ProjectType.File);
        HProjectIteration hProjectIteration = new HProjectIteration();
        hProjectIteration.setProject(hProject);
        hProjectIteration.setProjectType(ProjectType.File);
        HDocument hDocument = new HDocument();
        hDocument.setLocale(new HLocale(new LocaleId("en")));

        when(projectIterationDAO.getBySlug(project, version))
                .thenReturn(hProjectIteration);
        when(documentDAO.getByDocIdAndIteration(hProjectIteration, docId))
                .thenReturn(hDocument);
        when(documentDAO.getByProjectIterationAndDocId(project, version, docId))
                .thenReturn(hDocument);

        TranslationsResource translationsResource = transFileService
                .parseTranslationFile(stream, docId,
                        "ru", project, version, docId, Optional.absent());
        TextFlowTarget target = translationsResource.getTextFlowTargets().get(0);
        assertThat(target.getContents().get(0)).isEqualTo("test message");
        assertThat(target.getResId()).isEqualTo("first");
    }

    @Test
    @InRequestScope
    public void rejectNonPoInGettextProject() throws Exception {
        String project = "test";
        String version = "master";
        String docId = "test.pot";
        InputStream stream = new NullInputStream(0);
        HProject hProject = new HProject();
        hProject.setDefaultProjectType(ProjectType.Gettext);
        HProjectIteration hProjectIteration = new HProjectIteration();
        when(projectIterationDAO.getBySlug(project, version))
                .thenReturn(hProjectIteration);

        try {
            transFileService.parseTranslationFile(stream, docId,
                    "ru", project, version, docId, Optional.absent());
            fail("Expected a ZanataServiceException");
        } catch (ZanataServiceException zse) {
            assertThat(zse.getMessage()).contains("Unsupported Translation file");
        }
    }

}
