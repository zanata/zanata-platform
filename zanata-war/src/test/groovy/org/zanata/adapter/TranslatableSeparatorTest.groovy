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

import org.testng.annotations.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.greaterThan
import static org.hamcrest.Matchers.equalTo


class TranslatableSeparatorTest {

    @Test
    void emptyStringWorks() {
        def stripped = TranslatableSeparator.separate("")
        assertThat(stripped, equalTo([pre: "", str: "", suf: ""]))
    }

    @Test
    void plainTextNotStripped() {
        def original = "Some text"
        def stripped = TranslatableSeparator.separate(original)
        assertThat(stripped, equalTo([pre: "", str: original, suf: ""]))
    }

    @Test
    void leadingWhitespaceStripped() {
        def stripped = TranslatableSeparator.separate("  Some text")
        assertThat(stripped, equalTo([pre: "  ", str: "Some text", suf: ""]))
    }

    @Test
    void leadingNewlinesStripped() {
        def stripped = TranslatableSeparator.separate("\nSome text")
        assertThat(stripped, equalTo([pre: "\n", str: "Some text", suf: ""]))
    }

    @Test
    void leadingTabsStripped() {
        def stripped = TranslatableSeparator.separate("\tSome text")
        assertThat(stripped, equalTo([pre: "\t", str: "Some text", suf: ""]))
    }

    @Test
    void complexLeadingWhitespaceStripped() {
        def stripped = TranslatableSeparator.separate('''
                    Some text''')
        assertThat(stripped, equalTo([pre: '''
                    ''', str: "Some text", suf: ""]))
    }

    @Test
    void multilineWhitespaceStripped() {
        def original = '''
              Loading…
          '''
        def stripped = TranslatableSeparator.separate(original);
        assertThat(stripped, equalTo([pre: '''
              ''', str: 'Loading…', suf: '''
          ''']))
    }


    @Test
    void trailingWhitespaceStripped() {
        def stripped = TranslatableSeparator.separate("Some text  ")
        assertThat(stripped, equalTo([pre: "", str: "Some text", suf: "  "]))
    }

    @Test
    void leadingStandaloneTagsStripped() {
        def stripped = TranslatableSeparator.separate("<x1/><b2/><e3/>Some text")
        assertThat(stripped, equalTo([pre: "<x1/><b2/><e3/>", str: "Some text", suf: ""]))
    }

    @Test
    void trailingStandaloneTagsStripped() {
        def stripped = TranslatableSeparator.separate("Some text<b1/><e2 / ><x3/>")
        assertThat(stripped, equalTo([pre: "", str: "Some text", suf: "<b1/><e2 / ><x3/>"]))
    }

    @Test
    void wrappingTagsStripped() {
        def stripped = TranslatableSeparator.separate("<g1>Some text</g1>")
        assertThat(stripped, equalTo([pre: "<g1>", str: "Some text", suf:"</g1>"]))
    }

    @Test
    void nonWrappingTagsNotStripped() {
        def stripped = TranslatableSeparator.separate("<g1>Some</g1> <g2>text</g2>")
        assertThat(stripped, equalTo([pre: "", str: "<g1>Some</g1> <g2>text</g2>", suf:""]))
    }

    @Test
    void leadingEmptyPairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("<g1></g1>Some text")
        assertThat(stripped, equalTo([pre: "<g1></g1>", str: "Some text", suf:""]))
    }

    @Test
    void leadingNonTranslatablePairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("<g1>\n\t<x1/> </g1>Some text")
        assertThat(stripped, equalTo([pre: "<g1>\n\t<x1/> </g1>", str: "Some text", suf:""]))
    }

    @Test
    void leadingPairedTagsMatchedProperly() {
        def stripped = TranslatableSeparator.separate("<g1><g1></g1></g1>Some text")
        assertThat(stripped, equalTo([pre: "<g1><g1></g1></g1>", str: "Some text", suf:""]))
    }

    @Test
    void trailingEmptyPairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("Some text<g1></g1>")
        assertThat(stripped, equalTo([pre: "", str: "Some text", suf:"<g1></g1>"]))
    }

    @Test
    void trailingNonTranslatablePairedTagsStripped() {
        def stripped = TranslatableSeparator.separate("Some text<g1>\n\t<x1/> </g1>")
        assertThat(stripped, equalTo([pre: "", str: "Some text", suf:"<g1>\n\t<x1/> </g1>"]))
    }

    @Test
    void trailingPairedTagsMatchedProperly() {
        def stripped = TranslatableSeparator.separate("Some text<g1><g1></g1></g1>")
        assertThat(stripped, equalTo([pre: "", str: "Some text", suf:"<g1><g1></g1></g1>"]))
    }

    @Test
    void combinedWhitespaceAndTagsStripped() {
        def stripped = TranslatableSeparator.separate("\n\t\t<g2><x1/></g2>\n\t\t<g3>\n\t\t\tSome<x4/>\n\t\t\t<g5>text</g5>\n\t\t</g3>\n\t\t<e6/><b7/>\n")
        assertThat(stripped, equalTo([pre: "\n\t\t<g2><x1/></g2>\n\t\t<g3>\n\t\t\t", str: "Some<x4/>\n\t\t\t<g5>text</g5>", suf:"\n\t\t</g3>\n\t\t<e6/><b7/>\n"]))
    }
}
