package org.zanata.service.tm.merge

import org.zanata.common.ContentState
import org.zanata.common.ContentState.*
import java.util.*

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class TMMergeResult (private val bandDefs: Map<ContentState, List<IntRange>>): TMMergeTracker {

    private val bandCounters = HashMap<Pair<ContentState, IntRange>, MutableCounter>()

    init {
        // for each defined ContentState/IntRange pair, we hold a Counter
        for((contentState, ranges) in bandDefs) {
            ranges.forEach { r ->
                bandCounters[Pair(contentState, r)] = MutableCounter()
            }
        }
    }

    /**
     * Returns all ContentStates in the order we want to report them
     */
    val contentStates: List<ContentState>
        get() = listOf(Approved, Translated, NeedReview, Rejected, New)

    /**
     * Returns a list of IntRanges which together cover 0 to 100 for the specified ContentState.
     */
    fun rangesForContentState(state: ContentState): List<IntRange> =
            bandDefs[state]!!

    /**
     * Returns true if the counter for (state, range) has counted zero messages.
     */
    fun noMessagesCounted(state: ContentState, range: IntRange): Boolean =
            bandCounters[Pair(state, range)]?.messages == 0L

    /**
     * Returns true if and only if all the counters for 'state' have counted zero messages.
     */
    fun noMessagesCounted(state: ContentState): Boolean {
        for (range in rangesForContentState(state)) {
            if (bandCounters[Pair(state, range)]?.messages != 0L) return false
        }
        return true
    }

    /**
     * Returns a read-only counter for the specified (ContentState, IntRange)
     */
    fun getCounter(state: ContentState, range: IntRange): Counter =
        bandCounters[Pair(state, range)]!!

    override fun count(state: ContentState, score: Int, chars: Long, words: Long, messages: Long) {
        assert(messages >= 1)
        val ranges = bandDefs[state] ?: throw RuntimeException("missing bands for $state")
        val range = ranges.find { range -> range.contains(score) } ?: throw RuntimeException("missing band for $score")
        // find the right counter for ContentState+range
        val counterKey = Pair(state, range)
        val counter = bandCounters[counterKey] ?: throw RuntimeException("missing counter for $counterKey")
        counter.codePoints += chars
        counter.words += words
        counter.messages += messages
    }
}

interface TMMergeTracker {
    /**
     * Count one or more copied messages
     * @param state state of copied message
     * @param score similarity score of the match (in source language)
     * @param chars number of characters in the source language (Unicode code points)
     * @param words number of words in the source language
     * @param messages number of messages copied. Must be >= 1.
     */
    fun count(state: ContentState, score: Int, chars: Long, words: Long, messages: Long = 1)

    object NOOP: TMMergeTracker {
        override fun count(state: ContentState, score: Int, chars: Long, words: Long, messages: Long) {}
    }
}

interface Counter {
    val codePoints: Long
    val words: Long
    val messages: Long
}

private data class MutableCounter(override var codePoints: Long = 0, override var words: Long = 0, override var messages: Long = 0): Counter
