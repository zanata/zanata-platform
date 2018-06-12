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


import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;

import com.google.common.base.Optional;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
// TODO test writeTranslatedFile
public class PlainTextAdapterTest extends AbstractAdapterTest<PlainTextAdapter> {

    @Before
    public void setup() {
        adapter = new PlainTextAdapter();
    }

    @Test
    public void parseTextWithDefaultSettings() {
        Resource resource = parseTestFile("plaintext.txt");
        assertThat(resource.getTextFlows()).hasSize(3);
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("The first paragraph is split over more than one line. It has more than");
    }

    @Test
    public void parseTextWithParagraphExtraction() {
        // NB parameters copied from okapi-filter-plaintext: /net/sf/okapi/filters/plaintext/okf_plaintext_paragraphs.fprm
        // TODO we need a way of using Okapi's pre-defined filter configurations by name (okf_plaintext_paragraphs)
        Resource resource =
                adapter.parseDocumentFile(new FileFormatAdapter.ParserOptions(
                        getTestFile("plaintext.txt").toURI(),
                        LocaleId.EN,
                        "#v1\n" +
                                "unescapeSource.b=true\n" +
                                "trimLeading.b=false\n" +
                                "trimTrailing.b=false\n" +
                                "preserveWS.b=true\n" +
                                "useCodeFinder.b=false\n" +
                                "codeFinderRules=#v1$0a$count.i=2$0a$rule0=%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]$0a$rule1=(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v$0a$sample=$0a$useAllRulesWhenTesting.b=false\n" +
                                "wrapMode.i=0\n" +
                                "extractParagraphs.b=true\n" +
                                "parametersClass=net.sf.okapi.filters.plaintext.paragraphs.Parameters"));
        assertThat(resource.getTextFlows()).hasSize(2);
        assertThat(resource.getTextFlows().get(0).getContents())
                .containsExactly("The first paragraph is split over more than one line. " +
                        "It has more than\n" +
                        "one sentence.");
    }

}
