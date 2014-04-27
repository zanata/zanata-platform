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
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses a String query to generate an EditorFilter object.
 *
 * @author damason@redhat.com
 */
public class QueryParser {


    private static String[] keys = {
            "text",
            "resource-id",
            "last-modified-by",
            "last-modified-before",
            "last-modified-after",
            "source-comment",
            "translation-comment",
            "msgctxt"
    };

    private static final RegExp keyRegex, valRegex;
    static {
        String plainChar = "[^\"\\\\]",
                escapedChar = "\\\\",  // slash followed by any single character
                character = "(?:" + plainChar + "|" + escapedChar + ")",
                quotedText = "\"" + character + "*?\"", // any plain or escaped chars between quotes
                value = "(?:" + character + "|" + quotedText + ")",
                captureLeadingValue = "^(" + value + "*?)",
                orKeys = join(keys, "|"), // keys.join("|"),
                anyKey = "(?: (?:" + orKeys + "):)", // space, then key including colon
                keyOrEndOfLine = "(?:" + anyKey + "|$)",
                keyAndRemainderOfLine = "(" + keyOrEndOfLine + "(?:.*$)?)";

        keyRegex = RegExp.compile( "^\\s*(" + orKeys + "):(.*)$" ); // new RegExp( "^\\s*(" + orKeys + "):(.*)$" ),
        valRegex = RegExp.compile(captureLeadingValue + keyAndRemainderOfLine) ;// new RegExp(captureLeadingValue + keyAndRemainderOfLine);
    }

    public static EditorFilter parse(String query) {
        if (query == null) {
            return EditorFilter.ALL;
        }
        Map<String, String> o = parseRecursive(query, new HashMap<String, String>());
        return new EditorFilter(o.get("text"),
                o.get("resource-id"),
                o.get("last-modified-before"),
                o.get("last-modified-after"),
                o.get("last-modified-by"),
                o.get("source-comment"),
                o.get("translation-comment"),
                o.get("msgctxt")
        );
    }

    private static Map<String, String> parseRecursive(String query, Map<String, String> accumulator) {
        MatchResult keyMatch = keyRegex.exec(query);

        String key, remainder;
        if (keyMatch != null) {
            key = keyMatch.getGroup(1);
            remainder = keyMatch.getGroup(2);
        } else {
            int defaultKey = 0;
            key = keys[defaultKey];
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

        for (; pos<text.length();) {
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

    private static String join (String[] values, String separator) {
        String result = "";
        boolean first = true;
        for (String value : values) {
            result = result + (first ? "" : separator) + value;
            first = false;
        }
        return result;
    }
}
