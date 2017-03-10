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

import org.assertj.core.api.KotlinAssertions.assertThat
import org.junit.Test
import org.zanata.adapter.TranslatableSeparator.SplitString

class TranslatableSeparatorTest {

    @Test
    fun emptyStringWorks() {
        val stripped = TranslatableSeparator.separate("")
        assertThat(stripped).isEqualTo(SplitString("", "", ""))
    }

    @Test
    fun plainTextNotStripped() {
        val original = "Some text"
        val stripped = TranslatableSeparator.separate(original)
        assertThat(stripped).isEqualTo(SplitString("", original, ""))
    }

    @Test
    fun leadingWhitespaceStripped() {
        val stripped = TranslatableSeparator.separate("  Some text")
        assertThat(stripped).isEqualTo(SplitString("  ", "Some text", ""))
    }

    @Test
    fun leadingNewlinesStripped() {
        val stripped = TranslatableSeparator.separate("\nSome text")
        assertThat(stripped).isEqualTo(SplitString("\n", "Some text", ""))
    }

    @Test
    fun leadingTabsStripped() {
        val stripped = TranslatableSeparator.separate("\tSome text")
        assertThat(stripped).isEqualTo(SplitString("\t", "Some text", ""))
    }

    @Test
    fun mixedLeadingWhitespaceStripped() {
        val stripped = TranslatableSeparator.separate("\n                    Some text")
        assertThat(stripped).isEqualTo(SplitString("\n                    ", "Some text", ""))
    }

    @Test
    fun multilineWhitespaceStripped() {
        val original = "\n              Loading…\n          "
        val stripped = TranslatableSeparator.separate(original)
        assertThat(stripped).isEqualTo(SplitString("\n              ", "Loading…", "\n          "))
    }

    @Test
    fun trailingWhitespaceStripped() {
        val stripped = TranslatableSeparator.separate("Some text  ")
        assertThat(stripped).isEqualTo(SplitString("", "Some text", "  "))
    }

    @Test
    fun leadingStandaloneTagsStripped() {
        val stripped = TranslatableSeparator.separate("<x1/><b2/><e3/>Some text")
        assertThat(stripped).isEqualTo(SplitString("<x1/><b2/><e3/>", "Some text", ""))
    }

    @Test
    fun trailingStandaloneTagsStripped() {
        val stripped = TranslatableSeparator.separate("Some text<b1/><e2 / ><x3/>")
        assertThat(stripped).isEqualTo(SplitString("", "Some text", "<b1/><e2 / ><x3/>"))
    }

    @Test
    fun wrappingTagsStripped() {
        val stripped = TranslatableSeparator.separate("<g1>Some text</g1>")
        assertThat(stripped).isEqualTo(SplitString("<g1>", "Some text", "</g1>"))
    }

    @Test
    fun nonWrappingTagsNotStripped() {
        val stripped = TranslatableSeparator.separate("<g1>Some</g1> <g2>text</g2>")
        assertThat(stripped).isEqualTo(SplitString("", "<g1>Some</g1> <g2>text</g2>", ""))
    }

    @Test
    fun leadingEmptyPairedTagsStripped() {
        val stripped = TranslatableSeparator.separate("<g1></g1>Some text")
        assertThat(stripped).isEqualTo(SplitString("<g1></g1>", "Some text", ""))
    }

    @Test
    fun leadingNonTranslatablePairedTagsStripped() {
        val stripped = TranslatableSeparator.separate("<g1>\n\t<x1/> </g1>Some text")
        assertThat(stripped).isEqualTo(SplitString("<g1>\n\t<x1/> </g1>", "Some text", ""))
    }

    @Test
    fun leadingPairedTagsMatchedProperly() {
        val stripped = TranslatableSeparator.separate("<g1><g1></g1></g1>Some text")
        assertThat(stripped).isEqualTo(SplitString("<g1><g1></g1></g1>", "Some text", ""))
    }

    @Test
    fun trailingEmptyPairedTagsStripped() {
        val stripped = TranslatableSeparator.separate("Some text<g1></g1>")
        assertThat(stripped).isEqualTo(SplitString("", "Some text", "<g1></g1>"))
    }

    @Test
    fun trailingNonTranslatablePairedTagsStripped() {
        val stripped = TranslatableSeparator.separate("Some text<g1>\n\t<x1/> </g1>")
        assertThat(stripped).isEqualTo(SplitString("", "Some text", "<g1>\n\t<x1/> </g1>"))
    }

    @Test
    fun trailingPairedTagsMatchedProperly() {
        val stripped = TranslatableSeparator.separate("Some text<g1><g1></g1></g1>")
        assertThat(stripped).isEqualTo(SplitString("", "Some text", "<g1><g1></g1></g1>"))
    }

    @Test
    fun combinedWhitespaceAndTagsStripped() {
        val stripped = TranslatableSeparator.separate("\n\t\t<g2><x1/></g2>\n\t\t<g3>\n\t\t\tSome<x4/>\n\t\t\t<g5>text</g5>\n\t\t</g3>\n\t\t<e6/><b7/>\n")
        assertThat(stripped).isEqualTo(SplitString("\n\t\t<g2><x1/></g2>\n\t\t<g3>\n\t\t\t", "Some<x4/>\n\t\t\t<g5>text</g5>", "\n\t\t</g3>\n\t\t<e6/><b7/>\n"))
    }
}
