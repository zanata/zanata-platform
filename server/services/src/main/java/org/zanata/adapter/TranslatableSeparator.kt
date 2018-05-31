/*
 * Copyright 2013, 2015, Red Hat, Inc. and individual contributors
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

import java.util.function.Function
import java.util.regex.Pattern

/**
 * @see separate
 * @author David Mason, [damason@redhat.com](mailto:damason@redhat.com)
 * @author Sean Flanigan, [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
object TranslatableSeparator {

    data class SplitString(
            /** leading (non-translatable) portion  */
            val pre: String,
            /** translatable portion of string  */
            val str: String,
            /** trailing (non-translatable) portion  */
            val suf: String)

    /**
     * Separates leading and trailing non-translatable portions of a string from
     * the translatable portion.

     * The leading portion maps to 'pre', the trailing non-translatable portion
     * maps to 'suf' and the translatable portion maps to 'str'.
     */
    @JvmStatic
    fun separate(s: String): SplitString {
        val stripAllNonTranslatable = Function<SplitString, SplitString> { stripTrailingEmptyPairedTags(it) }
                .compose(Function<SplitString, SplitString> { stripLeadingEmptyPairedTags(it) })
                .compose(Function<SplitString, SplitString> { stripWrappingTags(it) })
                .compose(Function<SplitString, SplitString> { stripTrailingEmptyTag(it) })
                .compose(Function<SplitString, SplitString> { stripLeadingEmptyTags(it) })
                .compose(Function<SplitString, SplitString> { stripTrailingWhitespace(it) })
                .compose(Function<SplitString, SplitString> { stripLeadingWhitespace(it) })
        var preStripped = SplitString("", s, "")
        var stripped = stripAllNonTranslatable.apply(preStripped)
        while (preStripped != stripped) {
            preStripped = stripped
            stripped = stripAllNonTranslatable.apply(preStripped)
        }
        return stripped
    }

    internal fun stripLeadingWhitespace(it: SplitString): SplitString {
        val leadingWhitespace = "(?ms)^(\\s*)(.*)$"
        val matcher = Pattern.compile(leadingWhitespace).matcher(it.str)
        if (matcher.matches()) {
            return SplitString(
                    it.pre + matcher.group(1),
                    matcher.group(2),
                    it.suf)
        } else {
            return it
        }
    }

    // Does not treat whitespace-only string as having trailing whitespace
    internal fun stripTrailingWhitespace(it: SplitString): SplitString {
        val trailingWhitespace = "(?ms)^(.*[^\\s])(\\s*)$"
        val matcher = Pattern.compile(trailingWhitespace).matcher(it.str)
        if (matcher.matches()) {
            return SplitString(
                    it.pre,
                    matcher.group(1),
                    matcher.group(2) + it.suf)
        } else {
            return it
        }
    }

    internal fun stripLeadingEmptyTags(it: SplitString): SplitString {
        val leadingStandaloneTags = "(?ms)^((?:<[^/>]*/ ?>)*)(.*)$"
        val matcher = Pattern.compile(leadingStandaloneTags).matcher(it.str)
        if (matcher.matches()) {
            return SplitString(
                    it.pre + matcher.group(1),
                    matcher.group(2),
                    it.suf)
        } else {
            return it
        }
    }

    internal fun stripTrailingEmptyTag(it: SplitString): SplitString {
        val trailingStandaloneTag = "(?ms)^(.*)(<[^/>]*/\\s*>)$"
        val matcher = Pattern.compile(trailingStandaloneTag).matcher(it.str)
        if (matcher.matches()) {
            return SplitString(
                    it.pre,
                    matcher.group(1),
                    matcher.group(2) + it.suf)
        } else {
            return it
        }
    }

    @Suppress("MagicNumber")
    internal fun stripWrappingTags(it: SplitString): SplitString {
        val wrappingTags = "(?ms)^(<([^/>]*)\\s*>)(.*)(</\\2\\s*>)$"
        val matcher = Pattern.compile(wrappingTags).matcher(it.str)
        if (matcher.matches()) {
            return SplitString(
                    it.pre + matcher.group(1),
                    matcher.group(3),
                    matcher.group(4) + it.suf)
        } else {
            return it
        }
    }

    @Suppress("MagicNumber")
    internal fun stripLeadingEmptyPairedTags(it: SplitString): SplitString {
        val leadingPairedTags = "(?ms)^(<([^/>]*)\\s*>(.*)</\\2\\s*>)(.*)$"
        val matcher = Pattern.compile(leadingPairedTags).matcher(it.str)
        if (matcher.matches()) {
            val tagContents = matcher.group(3)
            val cleanedContents = separate(tagContents).str
            if (cleanedContents.isEmpty()) {
                return SplitString(
                        it.pre + matcher.group(1),
                        matcher.group(4),
                        it.suf)
            } else {
                return it
            }
        } else {
            return it
        }
    }

    @Suppress("MagicNumber")
    internal fun stripTrailingEmptyPairedTags(it: SplitString): SplitString {
        val trailingPairedTags = "(?ms)^(.*?)(<([^/>]*)\\s*>(.*)</\\3\\s*>)$"
        val matcher = Pattern.compile(trailingPairedTags).matcher(it.str)
        if (matcher.matches()) {
            val tagContents = matcher.group(4)
            val cleanedContents = separate(tagContents).str
            if (cleanedContents.isEmpty()) {
                return SplitString(
                        it.pre,
                        matcher.group(1),
                        matcher.group(2) + it.suf)
            } else {
                return it
            }
        } else {
            return it
        }
    }

}
