package org.zanata.service

import org.zanata.common.ContentState

interface TranslationCounter {
    /**
     * Count one or more copied messages
     * @param state state of copied message
     * @param score similarity score of the match (in source language)
     * @param chars number of characters in the source language (Unicode code points)
     * @param words number of words in the source language
     * @param messages number of messages copied. Must be >= 1.
     */
    fun count(state: ContentState, score: Int, chars: Long, words: Long, messages: Long = 1)

    object NOOP: TranslationCounter {
        override fun count(state: ContentState, score: Int, chars: Long, words: Long, messages: Long) {}
    }
}

interface TextFlowCounter {
    val codePoints: Long
    val words: Long
    val messages: Long
}
