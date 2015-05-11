/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.common;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.zanata.common.ProjectType.*;

public class ProjectTypeTest {

    @Test
    public void getValueOfCapitalizedStringsMatchesType() throws Exception {
        assertThat(getValueOf("Utf8Properties"), is(Utf8Properties));
        assertThat(getValueOf("Properties"), is(Properties));
        assertThat(getValueOf("Gettext"), is(Gettext));
        assertThat(getValueOf("Podir"), is(Podir));
        assertThat(getValueOf("Xliff"), is(Xliff));
        assertThat(getValueOf("Xml"), is(Xml));
        assertThat(getValueOf("File"), is(File));
    }

    @Test
    public void getValueOfLowercaseStringsMatchesType() throws Exception {
        assertThat(getValueOf("utf8properties"), is(Utf8Properties));
        assertThat(getValueOf("properties"), is(Properties));
        assertThat(getValueOf("gettext"), is(Gettext));
        assertThat(getValueOf("podir"), is(Podir));
        assertThat(getValueOf("xliff"), is(Xliff));
        assertThat(getValueOf("xml"), is(Xml));
        assertThat(getValueOf("file"), is(File));
    }

    @Test
    public void getValueOfRawSaysToUseFile() throws Exception {
        try {
            getValueOf("raw");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Project type 'raw' no longer supported, use 'File' instead"));
        }
    }

    @Test(expected = Exception.class)
    public void getValueOfEmptyStringThrowsException() throws Exception {
        getValueOf("");
    }

    @Test(expected = Exception.class)
    public void getValueOfUnknownTypeThrowsException() throws Exception {
        getValueOf("whatever");
    }

    @Test
    public void gettextProjectsSupportOnlyPot() {
        List<DocumentType> supported = getSupportedSourceFileTypes(Gettext);
        assertThat(supported.get(0).getSourceExtensions(), contains("pot"));
        assertThat(supported, hasSize(1));
    }

    @Test
    public void podirProjectsSupportOnlyPot() {
        List<DocumentType> supported = getSupportedSourceFileTypes(Podir);
        assertThat(supported.get(0).getSourceExtensions(), contains("pot"));
        assertThat(supported, hasSize(1));
    }

    @Test
    public void supportedSourceFileTypesCorrectForFileProject() {
        List<DocumentType> supportedTypes = getSupportedSourceFileTypes(File);
        Set<String> extensions = new HashSet<>();
        for (DocumentType docType: supportedTypes) {
            extensions.addAll(docType.getSourceExtensions());
        }
        assertThat(extensions,
            containsInAnyOrder("dtd", "txt", "idml", "htm", "html", "odt",
                "odp", "ods", "odg", "srt", "sbt", "sub", "vtt",
                "properties", "xml", "pot"));
    }

    @Test
    public void supportedSourceFileTypesEmptyForOtherProjectTypes() {
        // There is currently no DocumentType representation of the source documents for these project types
        // They should be added, but while they are absent we expect empty source type lists.
        assertThat(getSupportedSourceFileTypes(Properties), hasSize(0));
        assertThat(getSupportedSourceFileTypes(Utf8Properties), hasSize(0));
        assertThat(getSupportedSourceFileTypes(Xliff), hasSize(0));
        assertThat(getSupportedSourceFileTypes(Xml), hasSize(0));
    }
}
