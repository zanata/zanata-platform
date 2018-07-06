/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.feature.misc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.zanata.feature.testharness.BasicAcceptanceTest
import org.zanata.feature.testharness.DetailedTest

/**
 * If this test doesn't pass, Failsafe is not retrying failing tests as it should.
 *
 *
 * Make sure failsafe.rerunFailingTestsCount is at least 1.
 *
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@DetailedTest
class FlakyTest {

    @BasicAcceptanceTest
    @Test
    @Disabled("TODO")
    fun testFlaky() {
        if (n++ == 0) {
            throw AssertionError("deliberately flaky test (should pass the next time)")
        }
        assert(true)
    }

    companion object {
        internal var n = 0
    }
}
