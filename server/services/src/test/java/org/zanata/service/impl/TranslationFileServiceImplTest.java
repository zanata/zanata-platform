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
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.DocumentType;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
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
        File tempFile = File.createTempFile("test", ".po");
        Files.write(tempFile.toPath(), poContent.getBytes());
        InputStream stream = new FileInputStream(tempFile);

        HProject hProject = new HProject();
        hProject.setDefaultProjectType(ProjectType.File);
        HProjectIteration hProjectIteration = new HProjectIteration();
        hProjectIteration.setProject(hProject);
        HDocument hDocument = new HDocument();

        when(projectIterationDAO.getBySlug(project, version))
                .thenReturn(hProjectIteration);
        when(documentDAO.getByDocIdAndIteration(hProjectIteration, docId))
                .thenReturn(hDocument);

        TranslationsResource translationsResource = transFileService
                .parseTranslationFile(stream, tempFile.getName(),
                "ru", project, version, docId, Optional.absent());
        assertThat(translationsResource.getTextFlowTargets().get(0)
                .getContents().get(0)).isEqualTo("1 aoeuaouaou");
    }

}
