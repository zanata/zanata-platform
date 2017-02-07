/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class GettextAdapterTest {

    private static GettextAdapter adapter;
    private static File testFile;

    @Before
    public void setup() {
        adapter = new GettextAdapter();
        System.out.println("test before");
    }

    public static Resource parseTestFile(String filename) {
        String filePath = "src/test/resources/org/zanata/adapter/";
        testFile = new File(filePath.concat(filename));
        assert testFile.exists();
        Resource resource = adapter.parseDocumentFile(testFile.toURI(),LocaleId.EN,Optional.absent());
        return resource;
    }

    @Test
    public void parsePOT() {
        Resource resource = parseTestFile("test-gettext.pot");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of("Line One"));
    }

    @Test
    public void testGettextWithComment() {
        String testComments = "#  translator-comments\n" +
                "#. comments-given-by-programmer\n" +
                "#: reference\n" +
                "#, flag";
        Resource resource = parseTestFile("test-gettext-comments.pot");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of(testComments));
    }

    @Test
    public void testGettextPlurals(){
        String testPlural = "Plural-Forms: nplurals=2; plural=n == 1 ? 0 : 1;\n";
        Resource resource = parseTestFile("test-gettext-plurals.po");
        assertThat(resource.getTextFlows()).hasSize(2);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of(testPlural));
        assertThat(resource.getTextFlows().get(1).getContents()).isEqualTo(
                ImmutableList.of(testPlural));
    }

    @Test
    public void testGettextFuzzy(){
        String testFuzzy = "#, fuzzy";
        Resource resource = parseTestFile("test-gettext-fuzzy.po");
        assertThat(resource.getTextFlows()).hasSize(2);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of(testFuzzy));
    }


}
