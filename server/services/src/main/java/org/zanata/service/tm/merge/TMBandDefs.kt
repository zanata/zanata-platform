package org.zanata.service.tm.merge

import org.zanata.common.ContentState
import org.zanata.common.ContentState.*
import org.zanata.config.TMBands
import org.zanata.config.TMFuzzyBandsConfig
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

private val exactly100 = IntRange(100, 100)
private val zeroTo99 = IntRange(0, 99)
private val fullRange = listOf(IntRange(0, 100))

/**
 * Returns a normalised list of numbers, starting from 100 down to 0
 * @param origBands a list of numbers between 0 and 100
 */
private fun normaliseLowerBands(origBands: List<Int>): List<Int> {
    if (origBands.isEmpty()) return listOf(100, 0)
    for (band in origBands) {
        assert(band >= 0)
        assert(band <= 100)
    }
    val bands = origBands.sortedDescending().toMutableList()
    if (bands.first() < 100) bands.add(0, 100)
    if (bands.last() > 0) bands.add(0)
    return bands
}

/**
 * Parses a string containing a list of integers (between 0 and 100)
 * and returns a descending list of IntRanges which together cover exactly
 * 0 to 100.
 */
fun parseBands(bandConfig: String): List<IntRange> {
    val rawBands = bandConfig.split(Regex("[, ]+"))
            .filterNot(String::isEmpty)
            .map { it.toInt() }
    val bands = normaliseLowerBands(rawBands)
    var previous = 101
    val ranges = mutableListOf<IntRange>()
    for (lower in bands) {
        ranges.add(lower until previous)
        previous = lower
    }
    return ranges
}

/**
 * Creates a full set of band definitions, covering each ContentState, and with a list
 * of IntRanges for each ContentState which cover scores between 0 and 100.
 * @param fuzzyBands a descending list of IntRanges for Fuzzy copies which together
 * must cover exactly 0 to 100.
 */
fun createTMBands(fuzzyBands: List<IntRange>): Map<ContentState, List<IntRange>> {
    return mapOf(
            Approved to listOf(exactly100, zeroTo99),
            Translated to listOf(exactly100, zeroTo99),
            NeedReview to fuzzyBands,
            Rejected to fullRange,
            New to fullRange)
}

class TMBandDefsProducer {
    /**
     * Produces a full set of TM bands, given the application configuration for the Fuzzy bands as a String.
     */
    @Produces
    @ApplicationScoped
    @TMBands
    fun produce(@TMFuzzyBandsConfig config: String): Map<ContentState, List<IntRange>> {
        val fuzzyBands = parseBands(config)
        return createTMBands(fuzzyBands)
    }
}
