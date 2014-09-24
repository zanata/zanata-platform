/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.client.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class VersionComparatorTest {
    public static final int GREATER = 1;
    public static final int EQUAL = 0;
    public static final int LESS = -1;
    private VersionComparator comparator = new VersionComparator();

    @Test
    public void canCompareVersions() {
        assertThat(comparator.compare("3.3.1", "3.3"), equalTo(GREATER));
        assertThat(comparator.compare("3.3.1", "3.3.1"), equalTo(EQUAL));
        assertThat(comparator.compare("3.3.1", "3.3.2"), equalTo(LESS));
        assertThat(comparator.compare("3.3.1-SNAPSHOT", "3.3"), equalTo(GREATER));
        assertThat(comparator.compare("3.3.1-SNAPSHOT", "3.3-SNAPSHOT"), equalTo(GREATER));
        assertThat(comparator.compare("3.3.1-SNAPSHOT", "3.3.1"), equalTo(LESS));
        assertThat(comparator.compare("3.3.1-SNAPSHOT", "3.3.2-SNAPSHOT"), equalTo(LESS));
        assertThat(comparator.compare("3.3.1-SNAPSHOT", "3.3.1-SNAPSHOT"), equalTo(EQUAL));
    }

}
