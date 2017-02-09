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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 * @// TODO: 6/02/17 test ids
 */
@RunWith(Enclosed.class)
public class SubtitleAdapterTest {

    static abstract class AbstractSubtitleAdapterTest {
        private SubtitleAdapter adapter;

        @Before
        public void setup() {
            adapter = new SubtitleAdapter();
        }

        Resource parseTestFile(String filename) {
            String resPath = "src/test/resources/org/zanata/adapter/";
            File testFile = new File(resPath.concat(filename));
            assert testFile.exists();
            return adapter.parseDocumentFile(testFile.toURI(), LocaleId.EN,
                    Optional.absent());
        }
    }

    public static class SubtitleAdapterSingle extends AbstractSubtitleAdapterTest {

        @Test
        public void parseBasicSRT() {
            Resource resource = parseTestFile("test-srt.srt");
            assertThat(resource.getTextFlows()).hasSize(3);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of("Line One"));
        }

        /*
         * Entries that are of identical content must be individual textflows
         */
        @Test
        public void testSimilarEntriesAreIndividual() {
            String testText = "Exactly the same text";
            Resource resource = parseTestFile("test-srt-duplicated.srt");
            assertThat(resource.getTextFlows()).hasSize(2);
            assertThat(resource.getTextFlows().get(0).getId()).isNotEqualTo(
                    resource.getTextFlows().get(1).getId());
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testText));
            assertThat(resource.getTextFlows().get(1).getContents()).isEqualTo(
                    ImmutableList.of(testText));
        }

        /*
         * WebVtt entries may start with a label or number, which can be safely ignored
         */
        @Test
        public void testLabelledVtt() {
            String testText = "Test subtitle 1";
            Resource resource = parseTestFile("test-vtt-labelled.vtt");
            assertThat(resource.getTextFlows()).hasSize(2);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testText));
        }

        /*
         * Entries can contain extra information in the ID
         */
        @Test
        public void testSubtitleWithSettings() {
            String testString = "Data to the right of the timestamps are cue settings";
            String vttId = "00:01:03.000 --> 00:01:06.500 position:90% align:right size:35%";
            String srtId = "00:00:20,000 --> 00:00:22,000  X1:40 X2:600 Y1:20 Y2:50";

            Resource resource = parseTestFile("test-vtt-settings.vtt");
            assertThat(resource.getTextFlows()).hasSize(1);
            assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(vttId);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testString));

            resource = parseTestFile("test-srt-settings.srt");
            assertThat(resource.getTextFlows()).hasSize(1);
            assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(srtId);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testString));
        }

        /*
         * WebVtt may contain timestamps in the text
         */
        @Test
        public void testVttInlineTimestamp() {
            Resource resource = parseTestFile("test-vtt-inlinetimestamp.vtt");
            assertThat(resource.getTextFlows()).hasSize(1);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of("Inline:<x1/>Visible five seconds after start"));
        }
    }

    @RunWith(Parameterized.class)
    public static class MultilineSubtitle extends AbstractSubtitleAdapterTest {

        @Parameterized.Parameters
        public static Object[] data() {
            return new Object[]{
                    "test-srt-multiline.srt",
                    "test-vtt-multiline.vtt",
                    "test-sbt-multiline.sbt",
                    "test-sub-multiline.sub"};
        }

        private String fileName;

        public MultilineSubtitle(String file) {
            fileName = file;
        }

        /*
         * Entries may contain multiple lines, and be preserved as such
         */
        @Test
        public void testMultilineSubtitleFile() {
            String testText = "Test subtitle 1\nTest subtitle 1 line 2";
            Resource resource = parseTestFile(fileName);
            assertThat(resource.getTextFlows()).hasSize(2);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testText));
        }
    }

    @RunWith(Parameterized.class)
    public static class FormattedSubtitle extends AbstractSubtitleAdapterTest {

        @Parameterized.Parameters
        public static Object[] data() {
            return new Object[]{
                    "test-srt-formatted.srt",
                    "test-vtt-formatted.vtt",
                    "test-sbt-formatted.sbt",
                    "test-sub-formatted.sub" };
        }

        private String fileName;

        public FormattedSubtitle(String filename) {
            fileName = filename;
        }

        /*
         * Entries may contain formatting - underline, bold, etc
         */
        @Test
        public void testFormattedSubtitleFile() {
            String testText = "<x1/>Some text<x2/> {u}and more{/u}";
            Resource resource = parseTestFile(fileName);
            assertThat(resource.getTextFlows()).hasSize(1);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testText));
        }
    }
}
