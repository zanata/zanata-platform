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
package org.zanata.adapter;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see #separate(String)
 * @author David Mason,
 *         <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 * @author Sean Flanigan,
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TranslatableSeparator {

    public static final class SplitString {

        /**
         * leading (non-translatable) portion
         */
        public final String pre;

        /**
         * translatable portion of string
         */
        public final String str;

        /**
         * trailing (non-translatable) portion
         */
        public final String suf;

        @java.beans.ConstructorProperties({ "pre", "str", "suf" })
        public SplitString(final String pre, final String str,
                final String suf) {
            this.pre = pre;
            this.str = str;
            this.suf = suf;
        }

        /**
         * leading (non-translatable) portion
         */
        public String getPre() {
            return this.pre;
        }

        /**
         * translatable portion of string
         */
        public String getStr() {
            return this.str;
        }

        /**
         * trailing (non-translatable) portion
         */
        public String getSuf() {
            return this.suf;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof TranslatableSeparator.SplitString))
                return false;
            final SplitString other = (SplitString) o;
            final Object this$pre = this.getPre();
            final Object other$pre = other.getPre();
            if (this$pre == null ? other$pre != null
                    : !this$pre.equals(other$pre))
                return false;
            final Object this$str = this.getStr();
            final Object other$str = other.getStr();
            if (this$str == null ? other$str != null
                    : !this$str.equals(other$str))
                return false;
            final Object this$suf = this.getSuf();
            final Object other$suf = other.getSuf();
            if (this$suf == null ? other$suf != null
                    : !this$suf.equals(other$suf))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $pre = this.getPre();
            result = result * PRIME + ($pre == null ? 43 : $pre.hashCode());
            final Object $str = this.getStr();
            result = result * PRIME + ($str == null ? 43 : $str.hashCode());
            final Object $suf = this.getSuf();
            result = result * PRIME + ($suf == null ? 43 : $suf.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "TranslatableSeparator.SplitString(pre=" + this.getPre()
                    + ", str=" + this.getStr() + ", suf=" + this.getSuf() + ")";
        }
    }

    /**
     * Separates leading and trailing non-translatable portions of a string from
     * the translatable portion.
     *
     * The leading portion maps to 'pre', the trailing non-translatable portion
     * maps to 'suf' and the translatable portion maps to 'str'.
     */
    public static SplitString separate(String s) {
        Function<SplitString, SplitString> stripAllNonTranslatable =
                ((Function<SplitString, SplitString>) TranslatableSeparator::stripTrailingEmptyPairedTags)
                        .compose(
                                TranslatableSeparator::stripLeadingEmptyPairedTags)
                        .compose(TranslatableSeparator::stripWrappingTags)
                        .compose(TranslatableSeparator::stripTrailingEmptyTag)
                        .compose(TranslatableSeparator::stripLeadingEmptyTags)
                        .compose(TranslatableSeparator::stripTrailingWhitespace)
                        .compose(TranslatableSeparator::stripLeadingWhitespace);
        SplitString preStripped = new SplitString("", s, "");
        SplitString stripped = stripAllNonTranslatable.apply(preStripped);
        while (!preStripped.equals(stripped)) {
            preStripped = stripped;
            stripped = stripAllNonTranslatable.apply(preStripped);
        }
        return stripped;
    }

    static SplitString stripLeadingWhitespace(SplitString it) {
        String leadingWhitespace = "(?ms)^(\\s*)(.*)$";
        Matcher matcher = Pattern.compile(leadingWhitespace).matcher(it.str);
        if (matcher.matches()) {
            return new SplitString(it.pre + matcher.group(1), matcher.group(2),
                    it.suf);
        } else {
            return it;
        }
    }
    // Does not treat whitespace-only string as having trailing whitespace

    static SplitString stripTrailingWhitespace(SplitString it) {
        String trailingWhitespace = "(?ms)^(.*[^\\s])(\\s*)$";
        Matcher matcher = Pattern.compile(trailingWhitespace).matcher(it.str);
        if (matcher.matches()) {
            return new SplitString(it.pre, matcher.group(1),
                    matcher.group(2) + it.suf);
        } else {
            return it;
        }
    }

    static SplitString stripLeadingEmptyTags(SplitString it) {
        String leadingStandaloneTags = "(?ms)^((?:<[^/>]*/ ?>)*)(.*)$";
        Matcher matcher =
                Pattern.compile(leadingStandaloneTags).matcher(it.str);
        if (matcher.matches()) {
            return new SplitString(it.pre + matcher.group(1), matcher.group(2),
                    it.suf);
        } else {
            return it;
        }
    }

    static SplitString stripTrailingEmptyTag(SplitString it) {
        String trailingStandaloneTag = "(?ms)^(.*)(<[^/>]*/\\s*>)$";
        Matcher matcher =
                Pattern.compile(trailingStandaloneTag).matcher(it.str);
        if (matcher.matches()) {
            return new SplitString(it.pre, matcher.group(1),
                    matcher.group(2) + it.suf);
        } else {
            return it;
        }
    }

    static SplitString stripWrappingTags(SplitString it) {
        String wrappingTags = "(?ms)^(<([^/>]*)\\s*>)(.*)(</\\2\\s*>)$";
        Matcher matcher = Pattern.compile(wrappingTags).matcher(it.str);
        if (matcher.matches()) {
            return new SplitString(it.pre + matcher.group(1), matcher.group(3),
                    matcher.group(4) + it.suf);
        } else {
            return it;
        }
    }

    static SplitString stripLeadingEmptyPairedTags(SplitString it) {
        String leadingPairedTags = "(?ms)^(<([^/>]*)\\s*>(.*)</\\2\\s*>)(.*)$";
        Matcher matcher = Pattern.compile(leadingPairedTags).matcher(it.str);
        if (matcher.matches()) {
            String tagContents = matcher.group(3);
            String cleanedContents = separate(tagContents).str;
            if (cleanedContents.isEmpty()) {
                return new SplitString(it.pre + matcher.group(1),
                        matcher.group(4), it.suf);
            } else {
                return it;
            }
        } else {
            return it;
        }
    }

    static SplitString stripTrailingEmptyPairedTags(SplitString it) {
        String trailingPairedTags =
                "(?ms)^(.*?)(<([^/>]*)\\s*>(.*)</\\3\\s*>)$";
        Matcher matcher = Pattern.compile(trailingPairedTags).matcher(it.str);
        if (matcher.matches()) {
            String tagContents = matcher.group(4);
            String cleanedContents = separate(tagContents).str;
            if (cleanedContents.isEmpty()) {
                return new SplitString(it.pre, matcher.group(1),
                        matcher.group(2) + it.suf);
            } else {
                return it;
            }
        } else {
            return it;
        }
    }
}
