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
package org.zanata.util;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import com.google.common.primitives.Ints;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

/**
 * This object will be converted to JSON.
 * @see org.zanata.util.TestTracing
 *
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class TraceEntry {
    @SuppressWarnings("unused")
    private String testName;
    @SuppressWarnings("unused")
    private String summary;
    @SuppressWarnings("unused")
    private String testResult;
    @SuppressWarnings("unused")
    private List<Integer> testIds;
    @SuppressWarnings("unused")
    private List<Integer> planIds;

    TraceEntry() {
        // used by JSON serializer/deserializer
    }

    public TraceEntry(Trace trace, String displayName,
                      TestResult result) {
        testName = displayName;
        summary = trace.summary();
        testIds = toList(trace.testCaseIds());
        planIds = toList(trace.testPlanIds());
        testResult = result.name();
    }

    private static List<Integer> toList(int[] ints) {
        return Ints.asList(firstNonNull(ints, new int[0]));
    }

    public static enum TestResult {
        Passed, Failed, Ignored
    }
}
