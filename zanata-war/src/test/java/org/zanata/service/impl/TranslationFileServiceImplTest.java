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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.DocumentType;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.TranslationFileService;

@Test(groups = { "unit-tests" })
public class TranslationFileServiceImplTest {

    TranslationFileService transFileService;

    @BeforeMethod
    public void beforeTest() {
        transFileService =
                SeamAutowire.instance().reset().ignoreNonResolvable()
                        .autowire(TranslationFileServiceImpl.class);
    }

    // FIXME this is the current behaviour, but doesn't seem sensible
    @Test(enabled = true)
    public void extractExtensionFromPlainFilenameCurrentBehaviour() {
        assertThat(transFileService.extractExtension("foobar"), is("foobar"));
    }

    @Test(enabled = false)
    public void extractExtensionFromPlainFilenameBetterBehaviour() {
        assertThat(transFileService.extractExtension("foobar"), is(""));
    }

    public void extractNormalExtension() {
        assertThat(transFileService.extractExtension("file.txt"), is("txt"));
    }

    public void extractExtensionWithMultipleDots() {
        assertThat(transFileService.extractExtension("foo.bar.txt"), is("txt"));
    }

    public void extractFromSQLInjection() {
        String extension =
                transFileService
                        .extractExtension("file.txt;DROP ALL OBJECTS;other.txt");
        assertThat(extension, is("txt"));
    }

    public void hasPlainTextAdapter() {
        assertThat(transFileService.hasAdapterFor(DocumentType.PLAIN_TEXT),
                is(true));
    }

    public void generateSimpleDocId() {
        assertThat(transFileService.generateDocId("foo", "bar.txt"),
                is("foo/bar.txt"));
    }

    public void generateSQLInjectionDocId() {
        String sqlInjectFilename = "file.txt;DROP ALL OBJECTS;other.txt";
        assertThat(transFileService.generateDocId("", sqlInjectFilename),
                is(sqlInjectFilename));
    }
}
