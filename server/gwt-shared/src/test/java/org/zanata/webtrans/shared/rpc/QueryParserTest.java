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


import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;
import static org.zanata.webtrans.shared.rpc.QueryParser.joinValue;
import static org.zanata.webtrans.shared.rpc.QueryParser.parse;
import static org.zanata.webtrans.shared.rpc.QueryParser.stripQuotesAndEscapes;


public class QueryParserTest {

    private EditorFilter filter;

    @Before
    public void setup() {
        filter = null;
    }

    @Test
    public void stripQuotesLeavesPlainStringsUnchanged() {
        assertThat(stripQuotesAndEscapes("plain string"))
                .isEqualTo("plain string");
    }

    @Test
    public void stripQuotesRemovesPairedQuotes() {
        assertThat(stripQuotesAndEscapes("I \"really like\" quotes"))
                .isEqualTo("I really like quotes");
    }

    @Test
    public void stripQuotesRemovesUnpairedQuotes() {
        assertThat(stripQuotesAndEscapes("I \"sort of like quotes"))
                .isEqualTo("I sort of like quotes");
    }

    @Test
    public void stripQuotesRemovesEscapeCharacter() {
        assertThat(stripQuotesAndEscapes("We must \\e\\s\\c\\a\\p\\e from here"))
                .isEqualTo("We must escape from here");
    }

    @Test
    public void stripQuotesKeepsEscapedSlashCharacter() {
        assertThat(stripQuotesAndEscapes("Use \\\\ to escape any character"))
                .isEqualTo("Use \\ to escape any character");
    }

    @Test
    public void stripQuotesKeepsEscapedQuotes() {
        assertThat(stripQuotesAndEscapes("That's a \\\"really\\\" useful invention"))
                .isEqualTo("That's a \"really\" useful invention");
    }

    @Test
    public void joinValueAddsNewPair() {
        HashMap<String, String> accumulator = new HashMap<String, String>();
        joinValue(accumulator, "key1", "value1");
        assertThat(accumulator).containsOnly(entry("key1", "value1"));
    }

    @Test
    public void joinValueAppendsToExistingPair() {
        HashMap<String, String> accumulator = new HashMap<String, String>();
        accumulator.put("key1", "value1");
        joinValue(accumulator, "key1", "value2");
        assertThat(accumulator).containsOnly(entry("key1", "value1 value2"));
    }

    @Test
    public void joinValueIgnoresOtherKeys() {
        HashMap<String, String> accumulator = new HashMap<String, String>();
        accumulator.put("key1", "value1");
        joinValue(accumulator, "key2", "value2");
        assertThat(accumulator).containsOnly(entry("key1", "value1"), entry("key2", "value2"));
    }

    @Test
    public void simpleQueryUsesTextKey() {
        filter = parse("a simple search");
        assertThat(filter.getTextInContent()).isEqualTo("a simple search");
    }

    @Test
    public void unknownKeysAreJustText() {
        filter = parse("taxed: hello world last-muddified-by: lawyers");
        assertThat(filter.getTextInContent()).isEqualTo("taxed: hello world last-muddified-by: lawyers");
        assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
    }

    @Test
    public void keysWithoutColonAreJustText() {
        filter = parse("text hello world last-modified-by me");
        assertThat(filter.getTextInContent()).isEqualTo("text hello world last-modified-by me");
        assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
    }

    @Test
    public void singleKeySearchForText() {
        filter = parse("text: hello world");
        assertThat(filter.getTextInContent()).isEqualTo("hello world");
        assertFilterKeysNullOrEmptyExcept(filter, "text");
    }

    @Test
    public void singleKeySearchForResourceId() {
        filter = parse("resource-id: hello world");
        assertThat(filter.getResId()).isEqualTo("hello world");
        assertFilterKeysNullOrEmptyExcept(filter, "resource-id");
    }

    @Test
    public void singleKeySearchForLastModifiedBy() {
        filter = parse("last-modified-by: ricky_rouse");
        assertThat(filter.getLastModifiedByUser()).isEqualTo("ricky_rouse");
        assertFilterKeysNullOrEmptyExcept(filter, "last-modified-by");
    }

    @Test
    public void singleKeySearchForLastModifiedBefore() {
        filter = parse("last-modified-before: last-tuesday");
        assertThat(filter.getLastModifiedBefore()).isEqualTo("last-tuesday");
        assertFilterKeysNullOrEmptyExcept(filter, "last-modified-before");
    }

    @Test
    public void singleKeySearchForLastModifiedAfter() {
        filter = parse("last-modified-after: 1pm");
        assertThat(filter.getLastModifiedAfter()).isEqualTo("1pm");
        assertFilterKeysNullOrEmptyExcept(filter, "last-modified-after");
    }

    @Test
    public void singleKeySearchForSourceComment() {
        filter = parse("source-comment: translate this text");
        assertThat(filter.getSourceComment()).isEqualTo("translate this text");
        assertFilterKeysNullOrEmptyExcept(filter, "source-comment");
    }

    @Test
    public void singleKeySearchForTranslationComment() {
        filter = parse("translation-comment: I can't translate this nonsense");
        assertThat(filter.getTransComment()).isEqualTo("I can't translate this nonsense");
        assertFilterKeysNullOrEmptyExcept(filter, "translation-comment");
    }

    @Test
    public void singleKeySearchForMsgctxt() {
        filter = parse("msgctxt: it was in a file");
        assertThat(filter.getMsgContext()).isEqualTo("it was in a file");
        assertFilterKeysNullOrEmptyExcept(filter, "msgctxt");
    }

    @Test
    public void leadingTermsUseDefaultKey() {
        filter = parse("hello world last-modified-by: someone");
        assertThat(filter.getTextInContent()).isEqualTo("hello world");
        assertThat(filter.getLastModifiedByUser()).isEqualTo("someone");
    }

    @Test
    public void excessWhitespaceIsTrimmed() {
        filter = parse("    hello    world    ");
        assertThat(filter.getTextInContent()).isEqualTo("hello    world");
    }

    @Test
    public void quotedWhitespaceIsNotTrimmed() {
        filter = parse(" \"    hello    world    \" ");
        assertThat(filter.getTextInContent()).isEqualTo("    hello    world    ");
    }

    @Test
    public void escapeCharacterBlocksWhitespaceTrimming() {
        filter = parse("   \\  hello world  \\  ");
        assertThat(filter.getTextInContent()).isEqualTo("  hello world  ");
    }

    @Test
    public void nullQueryCanBeParsed() {
        filter = parse(null);
        assertAllFilterKeysNullOrEmpty(filter);
    }

    @Test
    public void emptyQueryCanBeParsed() {
        filter = parse("");
        assertAllFilterKeysNullOrEmpty(filter);
    }

    @Test
    public void keysWithNoValueCanBeParsed() {
        filter = parse("text: last-modified-by: source-comment:");
        assertAllFilterKeysNullOrEmpty(filter);
    }

    @Test
    public void valuesForDuplicateKeysAreJoinedWithSpace() {
        filter = parse("text: hello last-modified-by: me text: world");
        assertThat(filter.getTextInContent()).isEqualTo("hello world");
    }

    @Test
    public void spaceNotRequiredAfterKey() {
        filter = parse("last-modified-by:someone text:something");
        assertThat(filter.getTextInContent()).isEqualTo("something");
        assertThat(filter.getLastModifiedByUser()).isEqualTo("someone");
    }

    @Test
    public void spaceRequiredBeforeSubsequentKeys() {
        filter = parse("last-modified-by:someonetext:nothing");
        assertThat(filter.getTextInContent()).isNullOrEmpty();
        assertThat(filter.getLastModifiedByUser()).isEqualTo("someonetext:nothing");
    }

    @Test
    public void quotedKeysAreIgnored() {
        filter = parse("text: \" last-modified-by: me\" ");
        assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
        assertThat(filter.getTextInContent()).isEqualTo(" last-modified-by: me");
    }

    @Test
    public void escapedKeysAreIgnored() {
        filter = parse("text: \\last-modified-by: me");
        assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
        assertThat(filter.getTextInContent()).isEqualTo("last-modified-by: me");
    }

    @Test
    public void escapedColonKeysAreIgnored() {
        filter = parse("text: last-modified-by\\: me");
        assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
        assertThat(filter.getTextInContent()).isEqualTo("last-modified-by: me");
    }

    @Test
    public void escapedQuotesAreIgnored() {
        filter = parse("\"it was \\\"last-modified-by: me\"");
        assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
        assertThat(filter.getTextInContent()).isEqualTo("it was \"last-modified-by: me");
    }

    @Test
    public void lastModifiedByTakesSingleWord() {
        filter = parse("last-modified-by: me the search");
        assertThat(filter.getLastModifiedByUser()).isEqualTo("me");
        assertThat(filter.getTextInContent()).isEqualTo("the search");
    }

    @Test
    public void lastModifiedAfterTakesSingleWord() {
        filter = parse("last-modified-after: yesterday the search");
        assertThat(filter.getLastModifiedAfter()).isEqualTo("yesterday");
        assertThat(filter.getTextInContent()).isEqualTo("the search");
    }

    @Test
    public void lastModifiedBeforeTakesSingleWord() {
        filter = parse("last-modified-before: today the search");
        assertThat(filter.getLastModifiedBefore()).isEqualTo("today");
        assertThat(filter.getTextInContent()).isEqualTo("the search");
    }

    private static void assertAllFilterKeysNullOrEmpty(EditorFilter filter) {
        assertFilterKeysNullOrEmptyExcept(filter, "");
    }

    /**
     * assert that each key except the provided key has a null or empty value in the filter object
     */
    private static void assertFilterKeysNullOrEmptyExcept(EditorFilter filter, String exception) {
        if (!exception.equals("text")) {
            assertThat(filter.getTextInContent()).isNullOrEmpty();
        }
        if (!exception.equals("resource-id")) {
            assertThat(filter.getResId()).isNullOrEmpty();
        }
        if (!exception.equals("last-modified-by")) {
            assertThat(filter.getLastModifiedByUser()).isNullOrEmpty();
        }
        if (!exception.equals("last-modified-before")) {
            assertThat(filter.getLastModifiedBefore()).isNullOrEmpty();
        }
        if (!exception.equals("last-modified-after")) {
            assertThat(filter.getLastModifiedAfter()).isNullOrEmpty();
        }
        if (!exception.equals("source-comment")) {
            assertThat(filter.getSourceComment()).isNullOrEmpty();
        }
        if (!exception.equals("translation-comment")) {
            assertThat(filter.getTransComment()).isNullOrEmpty();
        }
        if (!exception.equals("msgctxt")) {
            assertThat(filter.getMsgContext()).isNullOrEmpty();
        }
    }
}
