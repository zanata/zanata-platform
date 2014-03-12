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

/**
 * @see #separate(String)
 */
class TranslatableSeparator {

    /**
     * Separates leading and trailing non-translatable portions of a string from
     * the translatable portion.
     *
     * The leading portion maps to 'pre', the trailing non-translatable portion
     * maps to 'suf' and the translatable portion maps to 'str'.
     */
    static Map<String, String> separate(String s) {

        def stripAllNonTranslatable = stripTrailingEmptyPairedTags << stripLeadingEmptyPairedTags  \
                       << stripWrappingTags \
                       << stripTrailingEmptyTag << stripLeadingEmptyTags \
                       << stripTrailingWhitespace << stripLeadingWhitespace

        def preStripped = [pre: "", str: s, suf: ""]
        def stripped = stripAllNonTranslatable(preStripped)
        while (preStripped != stripped) {
            preStripped = stripped
            stripped = stripAllNonTranslatable(preStripped)
        }

        // this is given to Java, so all Strings are converted to java.lang.String for compatibility
        [pre: stripped.pre.toString(), str: stripped.str.toString(), suf: stripped.suf.toString()]
    }

    static def stripLeadingWhitespace = {
        def leadingWhitespace = /(?ms)^(\s*)(.*)$/
        def matcher = ( it.str =~ leadingWhitespace )
        if (matcher.matches()) {
            [pre: it.pre + matcher[0][1], str: matcher[0][2], suf: it.suf]
        } else {
            it
        }
    }

    // Does not treat whitespace-only string as having trailing whitespace
    static def stripTrailingWhitespace = {
        def trailingWhitespace = /(?ms)^(.*[^\s])(\s*)$/
        def matcher = ( it.str =~ trailingWhitespace )
        if (matcher.matches()) {
            [pre: it.pre, str: matcher[0][1], suf: matcher[0][2] + it.suf]
        } else {
            it
        }
    }

    static def stripLeadingEmptyTags = {
        def leadingStandaloneTags = /(?ms)^((?:<[^\/>]*\/ ?>)*)(.*)$/
        def matcher = ( it.str =~ leadingStandaloneTags )
        if (matcher.matches()) {
            [pre: it.pre + matcher[0][1], str: matcher[0][2], suf: it.suf]
        } else {
            it
        }
    }

    static def stripTrailingEmptyTag = {
        def trailingStandaloneTag = /(?ms)^(.*)(<[^\/>]*\/\s*>)$/
        def matcher = ( it.str =~ trailingStandaloneTag )
        if (matcher.matches()) {
            [pre: it.pre, str: matcher[0][1], suf: matcher[0][2] + it.suf]
        } else {
            it
        }
    }

    static def stripWrappingTags = {
        def wrappingTags = /(?ms)^(<([^\/>]*)\s*>)(.*)(<\/\2\s*>)$/
        def matcher = ( it.str =~ wrappingTags )
        if (matcher.matches()) {
            [pre: it.pre + matcher[0][1], str: matcher[0][3], suf: matcher[0][4] + it.suf]
        } else {
            it
        }
    }

    static def stripLeadingEmptyPairedTags = {
        def leadingPairedTags = /(?ms)^(<([^\/>]*)\s*>(.*)<\/\2\s*>)(.*)$/
        def matcher = ( it.str =~ leadingPairedTags )
        if (matcher.matches()) {
            def tagContents = matcher[0][3];
            def cleanedContents = separate(tagContents).str
            if (cleanedContents.isEmpty()) {
                [pre: it.pre + matcher[0][1], str: matcher[0][4], suf: it.suf]
            } else {
                it
            }
        } else {
            it
        }
    }

    static def stripTrailingEmptyPairedTags = {
        def trailingPairedTags = /(?ms)^(.*?)(<([^\/>]*)\s*>(.*)<\/\3\s*>)$/
        def matcher = ( it.str =~ trailingPairedTags )
        if (matcher.matches()) {
            def tagContents = matcher[0][4];
            def cleanedContents = separate(tagContents).str
            if (cleanedContents.isEmpty()) {
                [pre: it.pre, str: matcher[0][1], suf: matcher[0][2] + it.suf]
            } else {
                it
            }
        } else {
            it
        }
    }

}
