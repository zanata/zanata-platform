/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.service.impl

import org.junit.Test

import org.assertj.core.api.Assertions.assertThat

class TranslationMemoryServiceImplUnitTest {
    @Test
    fun `escape text with keywords at start and middle`() {
        val escapedText = TranslationMemoryServiceImpl.escape(
                "AND one to two or three OR - (must) + be - true")
        assertThat(escapedText).isEqualTo(
                """"AND" one to two or three "OR" \- \(must\) \+ be \- true""")
    }

    @Test
    fun `escape text ending with keyword`() {
        val escapedText = TranslationMemoryServiceImpl.escape(
                "LIABLE TO YOU FOR ACTUAL, DIRECT, INDIRECT, CONSEQUENTIAL, PUNITIVE OR")
        assertThat(escapedText).isEqualTo(
                """LIABLE TO YOU FOR ACTUAL, DIRECT, INDIRECT, CONSEQUENTIAL, PUNITIVE "OR"""")
    }

    @Test
    fun `escape text with word 'FOR'`() {
        val escapedText = TranslationMemoryServiceImpl.escape(
                "LIABLE TO YOU FOR 1'; DROP TABLE 'Test'; /*")
        assertThat(escapedText).isEqualTo(
                """LIABLE TO YOU FOR 1'; DROP TABLE 'Test'; \/\*""")
    }

    @Test
    fun `escape text with word 'FOR' and keyword`() {
        val escapedText = TranslationMemoryServiceImpl.escape(
                "LIABLE TO YOU FOR ACTUAL, DIRECT, INDIRECT, CONSEQUENTIAL, PUNITIVE OR 1'; DROP TABLE 'Test'; /*")
        assertThat(escapedText).isEqualTo(
                """LIABLE TO YOU FOR ACTUAL, DIRECT, INDIRECT, CONSEQUENTIAL, PUNITIVE "OR" 1'; DROP TABLE 'Test'; \/\*""")
    }

    @Test
    fun `escape text with word 'FOR' and parens`() {
        val escapedText = TranslationMemoryServiceImpl.escape(
                "TEST FOR (DROP TABLE 'Test';)")
        assertThat(escapedText).isEqualTo(
                """TEST FOR \(DROP TABLE 'Test';\)""")
    }
}
