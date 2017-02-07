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
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.resource.Resource;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a> on 6/02/17.
 * @// TODO: 6/02/17 test ids
 */
public class SubtitleAdapterTest {

    private SubtitleAdapter adapter;
    private final String[] fileTypes = new String[]{"srt", "vtt", "sbt", "sub"};

    @Before
    public void setup() {
        adapter = new SubtitleAdapter();
    }

    private Resource setupTestFile(String filename) {
        String resPath = "src/test/resources/org/zanata/adapter/";
        File testFile = new File(resPath.concat(filename));
        assert testFile.exists();
        Resource resource =
                adapter.parseDocumentFile(testFile.toURI(), LocaleId.EN,
                        Optional.absent());
        //System.out.println(DTOUtil.toXML(resource));
        return resource;
    }

    @Test
    public void parseBasicSRT() {
        Resource resource = setupTestFile("test-srt.srt");
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
        Resource resource = setupTestFile("test-srt-duplicated.srt");
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
        Resource resource = setupTestFile("test-vtt-labelled.vtt");
        assertThat(resource.getTextFlows()).hasSize(2);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of(testText));
    }

    /*
     * Entries may contain multiple lines, and be preserved as such
     */
    @Test
    public void testMultilineSubtitleFile() {
        String testText = "Test subtitle 1\nTest subtitle 1 line 2";
        String targetFile = "test-$-multiline.$";
        for (String fileType : fileTypes) {
            Resource resource = setupTestFile(targetFile.replaceAll("\\$", fileType));
            assertThat(resource.getTextFlows()).hasSize(2);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testText));
        }
    }

    /*
     * Entries may contain formatting - underline, bold, etc
     */
    @Test
    public void testFormattedSubtitleFile() {
        String testText = "<x1/>Some text<x2/> {u}and more{/u}";
        String targetFile = "test-$-formatted.$";
        for (String fileType : fileTypes) {
            Resource resource = setupTestFile(targetFile.replaceAll("\\$", fileType));
            assertThat(resource.getTextFlows()).hasSize(1);
            assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                    ImmutableList.of(testText));
        }
    }

    /*
     * Entries can contain extra information in the ID
     */
    @Test
    public void testSubtitleWithSettings() {
        String testString = "Data to the right of the timestamps are cue settings";
        String vttId = "00:01:03.000 --> 00:01:06.500 position:90% align:right size:35%";
        String srtId = "00:00:20,000 --> 00:00:22,000  X1:40 X2:600 Y1:20 Y2:50";

        Resource resource = setupTestFile("test-vtt-settings.vtt");
        assertThat(resource.getTextFlows()).hasSize(1);
        assertThat(resource.getTextFlows().get(0).getId()).isEqualTo(vttId);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of(testString));

        resource = setupTestFile("test-srt-settings.srt");
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
        Resource resource = setupTestFile("test-vtt-inlinetimestamp.vtt");
        assertThat(resource.getTextFlows()).hasSize(1);
        assertThat(resource.getTextFlows().get(0).getContents()).isEqualTo(
                ImmutableList.of("Inline:<x1/>Visible five seconds after start"));
    }
}
