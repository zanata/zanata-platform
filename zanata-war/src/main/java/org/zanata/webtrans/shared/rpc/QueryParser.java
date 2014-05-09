/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.shared.rpc;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a String query to generate an EditorFilter object.
 *
 * @author damason@redhat.com
 */
public class QueryParser {

    private static final String TEXT_KEY = "text",
            RESID_KEY = "resource-id",
            LAST_MODIFIED_BY_KEY = "last-modified-by",
            LAST_MODIFIED_BEFORE_KEY = "last-modified-before",
            LAST_MODIFIED_AFTER_KEY = "last-modified-after",
            SOURCE_COMMENT_KEY = "source-comment",
            TRANSLATION_COMMENT_KEY = "translation-comment",
            MSGCTXT_KEY = "msgctxt";

    private static final String[] KEYS = {
            TEXT_KEY,
            RESID_KEY,
            LAST_MODIFIED_BY_KEY,
            LAST_MODIFIED_BEFORE_KEY,
            LAST_MODIFIED_AFTER_KEY,
            SOURCE_COMMENT_KEY,
            TRANSLATION_COMMENT_KEY,
            MSGCTXT_KEY
    };

    private static final int DEFAULT_KEY = 0;
    private static final List<String> KEYS_WITH_SINGLE_VALUE = Arrays.asList(
            LAST_MODIFIED_BY_KEY,
            LAST_MODIFIED_BEFORE_KEY,
            LAST_MODIFIED_AFTER_KEY
    );

    private static final RegExp keyRegex, valRegex, leadingWordRegex;

    static {
        String plainChar = "[^\"\\\\]",
                escapedChar = "\\\\",  // slash followed by any single character
                character = "(?:" + plainChar + "|" + escapedChar + ")",
                quotedText = "\"" + character + "*?\"", // any plain or escaped chars between quotes
                value = "(?:" + character + "|" + quotedText + ")",
                captureLeadingValue = "^(" + value + "*?)",
                orKeys = Joiner.on("|").skipNulls().join(KEYS),
                anyKey = "(?: (?:" + orKeys + "):)", // space, then key including colon
                keyOrEndOfLine = "(?:" + anyKey + "|$)",
                keyAndRemainderOfLine = "(" + keyOrEndOfLine + "(?:.*$)?)";

        keyRegex = RegExp.compile("^\\s*(" + orKeys + "):(.*)$");
        valRegex = RegExp.compile(captureLeadingValue + keyAndRemainderOfLine);

        String nonSpaceChar = "[^\"\\\\\\s]",
                nonSpaceOrEscapedChar = "(?:" + nonSpaceChar + "|" + escapedChar + ")";
        leadingWordRegex = RegExp.compile("^\\s*((?:" + nonSpaceOrEscapedChar + "+|" + quotedText + ")+)(.*)");
    }

    public static EditorFilter parse(String query) {
        if (query == null) {
            return EditorFilter.ALL;
        }
        Map<String, String> o = parseRecursive(query, new HashMap<String, String>());
        return new EditorFilter(o.get(TEXT_KEY),
                o.get(RESID_KEY),
                o.get(LAST_MODIFIED_BEFORE_KEY),
                o.get(LAST_MODIFIED_AFTER_KEY),
                o.get(LAST_MODIFIED_BY_KEY),
                o.get(SOURCE_COMMENT_KEY),
                o.get(TRANSLATION_COMMENT_KEY),
                o.get(MSGCTXT_KEY)
        );
    }

    private static Map<String, String> parseRecursive(String query, Map<String, String> accumulator) {
        MatchResult keyMatch = keyRegex.exec(query);

        String key, remainder;
        if (keyMatch != null) {
            key = keyMatch.getGroup(1);
            remainder = keyMatch.getGroup(2);
        } else {
            key = KEYS[DEFAULT_KEY];
            remainder = query;
        }

        MatchResult valMatch = valRegex.exec(remainder);
        String val, nextPart;
        if (valMatch != null) {
            val = valMatch.getGroup(1);
            nextPart = valMatch.getGroup(2);
        } else {
            val = remainder;
            nextPart = "";
        }

        if (KEYS_WITH_SINGLE_VALUE.contains(key)) {
            MatchResult wordMatch = leadingWordRegex.exec(val);
            if (wordMatch != null) {
                val = wordMatch.getGroup(1);
                String otherWords = wordMatch.getGroup(2);
                if (!Strings.isNullOrEmpty(otherWords.trim())) {
                    joinValue(accumulator, KEYS[DEFAULT_KEY], otherWords);
                }
            }
        }

        joinValue(accumulator, key, val);

        // stop if there is no more text to parse
        if (nextPart.trim().length() == 0) {
            return accumulator;
        }

        // defensive statement to prevent an infinite recursion if no progress has been made
        if (nextPart.length() == query.length()) {
            Log.error("infinite loop detected, aborting");
            return accumulator;
        }

        return parseRecursive(nextPart, accumulator);
    }

    protected static void joinValue(Map<String, String> accumulator, String key, String val) {
        String oldValue = accumulator.get(key);
        String newValue = stripQuotesAndEscapes(val.trim());
        if (oldValue == null) {
            accumulator.put(key, newValue);
        } else {
            accumulator.put(key, oldValue + " " + newValue);
        }
    }

    protected static String stripQuotesAndEscapes(String text) {
        int pos = 0;
        char character;

        while (pos < text.length()) {
            character = text.charAt(pos);
            if (character == '\\' || character == '"') {
                // strip out the character
                text = text.substring(0, pos) + text.substring(pos + 1);
            }
            if (character != '"') {
                // advance to next character, or if an escape character was stripped
                // skip over the character that was escaped
                pos++;
            }
        }
        return text;
    }

    public static String toQueryString(EditorFilter editorFilter) {
        StringBuilder sb = new StringBuilder();
        addIfNotEmpty(sb, TEXT_KEY, editorFilter.getTextInContent());
        addIfNotEmpty(sb, RESID_KEY, editorFilter.getResId());
        addIfNotEmpty(sb, LAST_MODIFIED_BY_KEY, editorFilter.getLastModifiedByUser());
        addIfNotEmpty(sb, LAST_MODIFIED_BEFORE_KEY, editorFilter.getLastModifiedBefore());
        addIfNotEmpty(sb, LAST_MODIFIED_AFTER_KEY, editorFilter.getLastModifiedAfter());
        addIfNotEmpty(sb, SOURCE_COMMENT_KEY, editorFilter.getSourceComment());
        addIfNotEmpty(sb, TRANSLATION_COMMENT_KEY, editorFilter.getTransComment());
        addIfNotEmpty(sb, MSGCTXT_KEY, editorFilter.getMsgContext());
        return sb.toString();
    }

    private static void addIfNotEmpty(StringBuilder stringBuilder, String key,
            String value) {
        if (!Strings.isNullOrEmpty(value)) {
            stringBuilder.append(key).append(":").append(value).append(" ");
        }
    }
}
