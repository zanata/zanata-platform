/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.adapter.po;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class PoWriter2Test {
    private static final String UTF_8 = "UTF-8";
    private PoWriter2 writer;

    private String contentOfResource(String name) {
        URL resource = getClass().getResource(name);
        assertThat(resource).as("missing test resource").isNotNull();
        return contentOf(resource, UTF_8);
    }

    private Resource createDoc() {
        Resource doc = new Resource("docname");
        TextFlow tf1 = new TextFlow("msgid1\tONE");
        tf1.setContents("msgid1\tONE");
        doc.getTextFlows().add(tf1);
        TextFlow tf2 = new TextFlow("msgid2\tTWO");
        tf2.setContents("msgid2\tTWO");
        doc.getTextFlows().add(tf2);
        return doc;
    }

    private Resource createInvalidDoc() {
        Resource doc = createDoc();
        // an invalid textflow with multiple contents but no plural flag
        doc.getTextFlows().get(0).setContents("msgid1\tONE", "content2");
        return doc;
    }

    private TranslationsResource createTranslations() {
        TranslationsResource doc = new TranslationsResource();
        List<TextFlowTarget> targets = doc.getTextFlowTargets();
        TextFlowTarget tft1 = new TextFlowTarget("msgid1\tONE");
        tft1.setState(ContentState.Approved);
        tft1.setContents("Translation 1\tONE");
        targets.add(tft1);
        TextFlowTarget tft2 = new TextFlowTarget("msgid2\tTWO");
        tft2.setState(ContentState.Translated);
        tft2.setContents("Translation 2\tTWO");
        targets.add(tft2);
        return doc;
    }

    @Test
    public void writePot() throws Exception {
        // given
        writer = new PoWriter2.Builder().create();
        Resource doc = createDoc();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        writer.writePot(out, UTF_8, doc);

        // then
        String result = out.toString(UTF_8);
        assertThat(result).isEqualTo(contentOfResource("writePot.pot"));
    }

    @Test
    public void writeInvalidPotWithContinueAfterError() throws Exception {
        // given
        writer = new PoWriter2.Builder().continueAfterError(true).create();

        Resource doc = createInvalidDoc();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        writer.writePot(out, UTF_8, doc);

        // then
        String result = out.toString(UTF_8);
        // if you omit the extra plural, the result is the same as writePot()
        assertThat(result).isEqualTo(contentOfResource("writePot.pot"));
    }

    @Test
    public void writeInvalidPotWithoutContinueAfterError() {
        // given
        writer = new PoWriter2.Builder().continueAfterError(false).create();

        Resource doc = createInvalidDoc();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        assertThatThrownBy(() -> writer.writePot(out, UTF_8, doc))
                // then
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("textflow has no plural flag but multiple plural forms");
    }

    @Test
    public void writeWithDefaultOptions() throws Exception {
        // given
        writer = new PoWriter2.Builder().create();
        Resource srcDoc = createDoc();
        TranslationsResource doc = createTranslations();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        writer.writePo(out, UTF_8, srcDoc, doc);

        // then
        String result = out.toString(UTF_8);
        assertThat(result).isEqualTo(contentOfResource("writeWithDefaultOptions.po"));
    }

    @Test
    public void writeEncodedTabs() throws Exception {
        // given
        writer = new PoWriter2.Builder().encodeTabs(true).create();
        Resource srcDoc = createDoc();
        TranslationsResource doc = createTranslations();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        writer.writePo(out, UTF_8, srcDoc, doc);

        // then
        String result = out.toString(UTF_8);
        String backslashT = "\\\\t";
        assertThat(result).isEqualTo(contentOfResource("writeWithDefaultOptions.po").replaceAll("\t",
                backslashT));
    }

    @Test
    public void writeOfflinePo() throws Exception {
        // given
        writer = new PoWriter2.Builder().mapIdToMsgctxt(true).create();

        Resource srcDoc = createDoc();
        TranslationsResource doc = createTranslations();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        writer.writePo(out, UTF_8, srcDoc, doc);

        // then
        String result = out.toString(UTF_8);
        assertThat(result).isEqualTo(contentOfResource("writeOfflinePo.po"));
    }

    @Test
    public void writeApprovedOnly() throws Exception {
        // given
        writer = new PoWriter2.Builder().approvedOnly(true).create();
        Resource srcDoc = createDoc();
        TranslationsResource doc = createTranslations();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // when
        writer.writePo(out, UTF_8, srcDoc, doc);

        // then
        String result = out.toString(UTF_8);
        assertThat(result).isEqualTo(contentOfResource("writeApprovedOnly.po"));
    }

}
