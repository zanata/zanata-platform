/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util

import org.codehaus.jackson.annotate.JsonAutoDetect
import com.google.common.primitives.Ints

import org.apache.commons.lang3.ObjectUtils.firstNonNull

/**
 * This object will be converted to JSON.
 * @see org.zanata.util.TestTracing
 *
 *
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
class TraceEntry {
    private lateinit var testName: String
    private lateinit var summary: String
    private lateinit var testResult: String
    private lateinit var testIds: List<Int>
    private lateinit var planIds: List<Int>

    @Suppress("unused")
    internal constructor() {
        // used by JSON serializer/deserializer
    }

    constructor(trace: Trace, displayName: String,
                result: TestResult) {
        testName = displayName
        summary = trace.summary
        testIds = toList(trace.testCaseIds)
        planIds = toList(trace.testPlanIds)
        testResult = result.name
    }

    private fun toList(ints: IntArray): List<Int> {
        return Ints.asList(*firstNonNull(ints, IntArray(0)))
    }

    enum class TestResult {
        Passed, Failed, Ignored
    }
}
