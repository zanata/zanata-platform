package org.zanata.email

import org.assertj.core.api.KotlinAssertions.assertThat
import org.junit.Test
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

}

private val exactly100 = IntRange(100, 100)
