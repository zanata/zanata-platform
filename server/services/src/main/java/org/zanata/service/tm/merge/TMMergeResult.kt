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

    fun countCopy(contentState: ContentState, similarity: Int, stats: MessageStats) {
        val ranges: List<IntRange>? = bandDefs[contentState]
        val counter = if (ranges != null) {
            val range = ranges.find { range -> range.contains(similarity) }
            // if there is a defined band, use its Stats
            range?.let { r -> bandCounters[contentState to r] }
                    // otherwise use default Stats
                    ?: otherCounter(contentState)
        } else {
            // no ranges for this ContentState, use default Stats
            otherCounter(contentState)
        }
        counter.codePoints += stats.codePoints
        counter.words += stats.words
        counter.messages += stats.messages
    }

}

private data class Counter(var codePoints: Long = 0, var words: Long = 0, var messages: Long = 0)

data class MessageStats(val codePoints: Long, val words: Long, val messages: Long = 1)

