package org.zanata.service.tm.merge

import org.zanata.common.ContentState

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class TMMergeResult (private val bandDefs: Map<ContentState, List<IntRange>>) {
    private val bandCounters = HashMap<Pair<ContentState, IntRange>, Counter>()
    private val otherCounters = HashMap<ContentState, Counter>()
    init {
        // for each defined ContentState/IntRange pair, we hold a Counter
        for((contentState, ranges) in bandDefs) {
            ranges.forEach { r ->
                bandCounters[contentState to r] = Counter()
            }
        }
    }

    // FIXME return all keys
    val states: Set<ContentState>
//        get() {return bandCounters.keys + otherCounters.keys}
        get() {return emptySet()}

    private fun otherCounter(contentState: ContentState) =
            otherCounters.getOrPut(contentState, { Counter() })

    /**
     * Count one or more copied messages
     * @param state state of copied message
     * @param score similarity score of the match (in source language)
     * @param chars number of characters in the source language (Unicode code points)
     * @param words number of words in the source language
     * @param messages number of messages copied
     */
    @JvmOverloads
    fun count(state: ContentState, score: Int,
              chars: Long, words: Long, messages: Long = 1) {
        val ranges: List<IntRange>? = bandDefs[state]
        // find the right counter for ContentState+range
        val counter = if (ranges != null) {
            val range = ranges.find { range -> range.contains(score) }
            // if there is a defined band, use its Stats
            range?.let { r -> bandCounters[state to r] }
                    // otherwise use default Stats
                    ?: otherCounter(state)
        } else {
            // no ranges for this ContentState, use default Stats
            otherCounter(state)
        }
        counter.codePoints += chars
        counter.words += words
        counter.messages += messages
    }

}

private data class Counter(var codePoints: Long = 0, var words: Long = 0, var messages: Long = 0)

