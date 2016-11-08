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
package org.zanata.adapter

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import org.zanata.adapter.TranslatableSeparator.SplitString;


class TranslatableSeparatorTest {

    @Test
    void emptyStringWorks() {
        def stripped = TranslatableSeparator.separate("")
        assertThat(stripped, equalTo(new SplitString("", "", "")))
    }

    @Test
    void plainTextNotStripped() {
        def original = "Some text"
        def stripped = TranslatableSeparator.separate(original)
        assertThat(stripped, equalTo(new SplitString("", original, "")))
    }

    @Test
    void leadingWhitespaceStripped() {
        def stripped = TranslatableSeparator.separate("  Some text")
        assertThat(stripped, equalTo(new SplitString("  ", "Some text", "")))
    }

    @Test
    void leadingNewlinesStripped() {
        def stripped = TranslatableSeparator.separate("\nSome text")
        assertThat(stripped, equalTo(new SplitString("\n", "Some text", "")))
    }

    @Test
    void leadingTabsStripped() {
        def stripped = TranslatableSeparator.separate("\tSome text")
        assertThat(stripped, equalTo(new SplitString("\t", "Some text", "")))
    }

    @Test
    void complexLeadingWhitespaceStripped() {
        def stripped = TranslatableSeparator.separate("\n                    Some text")
        assertThat(stripped, equalTo(new SplitString("\n                    ", "Some text", "")))
    }

    @Test
    void multilineWhitespaceStripped() {
        def original = "\n              Loading…\n          "
        def stripped = TranslatableSeparator.separate(original);
        assertThat(stripped, equalTo(new SplitString("\n              ", 'Loading…', "\n          ")))
    }


    @Test
    void trailingWhitespaceStripped() {
        def stripped = TranslatableSeparator.separate("Some text  ")
        assertThat(stripped, equalTo(new SplitString("", "Some text", "  ")))
    }

    @Test
    void leadingStandaloneTagsStripped() {
        def stripped = TranslatableSeparator.separate("<x1/><b2/><e3/>Some text")
        assertThat(stripped, equalTo(new SplitString("<x1/><b2/><e3/>", "Some text", "")))
    }

    @Test
    void trailingStandaloneTagsStripped() {
        def stripped = TranslatableSeparator.separate("Some text<b1/><e2 / ><x3/>")
        assertThat(stripped, equalTo(new SplitString("", "Some text", "<b1/><e2 / ><x3/>")))
    }

    @Test
    void wrappingTagsStripped() {
        def stripped = TranslatableSeparator.separate("<g1>Some text</g1>")
        assertThat(stripped, equalTo(new SplitString("<g1>", "Some text", "</g1>")))
    }

    @Test
    void nonWrappingTagsNotStripped() {
        def stripped = TranslatableSeparator.separate("<g1>Some</g1> <g2>text</g2>")
        assertThat(stripped, equalTo(new SplitString("", "<g1>Some</g1> <g2>text</g2>", "")))
    }

    @Test
    void leadingEmptyPairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("<g1></g1>Some text")
        assertThat(stripped, equalTo(new SplitString("<g1></g1>", "Some text", "")))
    }

    @Test
    void leadingNonTranslatablePairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("<g1>\n\t<x1/> </g1>Some text")
        assertThat(stripped, equalTo(new SplitString("<g1>\n\t<x1/> </g1>", "Some text", "")))
    }

    @Test
    void leadingPairedTagsMatchedProperly() {
        def stripped = TranslatableSeparator.separate("<g1><g1></g1></g1>Some text")
        assertThat(stripped, equalTo(new SplitString("<g1><g1></g1></g1>", "Some text", "")))
    }

    @Test
    void trailingEmptyPairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("Some text<g1></g1>")
        assertThat(stripped, equalTo(new SplitString("", "Some text", "<g1></g1>")))
    }

    @Test
    void trailingNonTranslatablePairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("Some text<g1>\n\t<x1/> </g1>")
        assertThat(stripped, equalTo(new SplitString("", "Some text", "<g1>\n\t<x1/> </g1>")))
    }

    @Test
    void trailingPairedTagsMatchedProperly() {
        def stripped = TranslatableSeparator.separate("Some text<g1><g1></g1></g1>")
        assertThat(stripped, equalTo(new SplitString("", "Some text", "<g1><g1></g1></g1>")))
    }

    @Test
    void combinedWhitespaceAndTagsStripped() {
        def stripped = TranslatableSeparator.separate("\n\t\t<g2><x1/></g2>\n\t\t<g3>\n\t\t\tSome<x4/>\n\t\t\t<g5>text</g5>\n\t\t</g3>\n\t\t<e6/><b7/>\n")
        assertThat(stripped, equalTo(new SplitString("\n\t\t<g2><x1/></g2>\n\t\t<g3>\n\t\t\t", "Some<x4/>\n\t\t\t<g5>text</g5>", "\n\t\t</g3>\n\t\t<e6/><b7/>\n")))
    }
}
