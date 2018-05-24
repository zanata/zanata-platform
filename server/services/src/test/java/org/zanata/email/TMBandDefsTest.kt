package org.zanata.email

import org.assertj.core.api.KotlinAssertions.assertThat
import org.junit.Test
import org.zanata.common.ContentState
import org.zanata.service.tm.merge.createTMBands
import org.zanata.service.tm.merge.parseBands

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class TMBandDefsTest {
    @Test
    fun `default ranges for empty config`() {
        val bands = parseBands("")
        assertThat(bands).isEqualTo(listOf(exactly100, 0 until 100))
    }

    @Test
    fun `one value`() {
        val bands = parseBands("90")
        assertThat(bands).isEqualTo(listOf(exactly100, 90 until 100, 0 until 90))
    }

    @Test
    fun `two values`() {
        val bands = parseBands("80 90")
        assertThat(bands).isEqualTo(listOf(exactly100, 90 until 100, 80 until 90, 0 until 80))
    }

    @Test
    fun `include 0 and 100`() {
        val bands = parseBands("100, 80, 90, 0")
        assertThat(bands).isEqualTo(listOf(exactly100, 90 until 100, 80 until 90, 0 until 80))
    }

    @Test
    fun `creates bands for each ContentState`() {
        val bandDefs: Map<ContentState, List<IntRange>> = createTMBands(parseBands("80"))
        assertThat(bandDefs.keys.toSet()).isEqualTo(ContentState.values().toSet())
    }

    @Test
    fun `creates full ranges for each ContentState`() {
        val bandDefs: Map<ContentState, List<IntRange>> = createTMBands(parseBands("80"))
        bandDefs.values.forEach { ranges ->
            assertThat(ranges.first().endInclusive).isEqualTo(100)
            assertThat(ranges.last().start).isEqualTo(0)
            assertThat(ranges).allMatch { it.first in 0..100 }
            assertThat(ranges).allMatch { it.last in 0..100 }
            // we could also check that ranges cover from 100 down to 0, and don't overlap
        }
    }

}

private val exactly100 = IntRange(100, 100)
