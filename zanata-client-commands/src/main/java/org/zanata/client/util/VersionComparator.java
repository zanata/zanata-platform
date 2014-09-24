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

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * This class will compare version strings i.e. 3.3.1 to 3.3. It can also
 * compare maven snapshot version i.e. 3.3.1 is newer than 3.3.1-SNAPSHOT.
 * It can NOT handle version like 1.1a or 1.1.Final or 1.1.Alpha.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class VersionComparator implements Comparator<String> {

    private static final Splitter splitter =
            Splitter.on(".").omitEmptyStrings().trimResults();
    public static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    @Override
    public int compare(String left, String right) {
        Preconditions.checkNotNull(left);
        Preconditions.checkNotNull(right);

        List<String> leftOrdinals =
                ImmutableList.copyOf(splitter.split(left.replace(
                        SNAPSHOT_SUFFIX, "")));
        List<String> rightOrdinals =
                ImmutableList.copyOf(splitter.split(right.replace(
                        SNAPSHOT_SUFFIX, "")));
        int compareIndex =
                firstDiffOrdinalOrLastOrdinalInShortestVer(leftOrdinals,
                        rightOrdinals);
        // compare first different ordinal
        if (compareIndex < leftOrdinals.size()
                && compareIndex < rightOrdinals.size()) {
            Integer leftOrdinal =
                    getOrdinalAsInteger(leftOrdinals, compareIndex);
            Integer rightOrdinal =
                    getOrdinalAsInteger(rightOrdinals, compareIndex);

            int ordinalDiff = leftOrdinal.compareTo(rightOrdinal);
            if (ordinalDiff == 0) {
                return compareSnapshot(left, right);
            }
            return ordinalDiff;
        }

        // either equal or one is a substring of another
        int sizeDiff = leftOrdinals.size() - rightOrdinals.size();
        if (sizeDiff == 0) {
            return compareSnapshot(left, right);
        }
        return Integer.signum(sizeDiff);
    }

    private static int firstDiffOrdinalOrLastOrdinalInShortestVer(
            List<String> leftOrdinals,
            List<String> rightOrdinals) {
        int result = 0;
        while (result < leftOrdinals.size() && result < rightOrdinals.size()
                && leftOrdinals.get(result).equals(rightOrdinals.get(result))) {
            result++;
        }
        return result;
    }

    private static Integer getOrdinalAsInteger(List<String> ordinals,
            int index) {
        return Integer.valueOf(ordinals.get(index));
    }

    private static int compareSnapshot(String left, String right) {
        boolean leftIsSnapshot = left.endsWith(SNAPSHOT_SUFFIX);
        boolean rightIsSnapshot = right.endsWith(SNAPSHOT_SUFFIX);
        if (leftIsSnapshot && !rightIsSnapshot) {
            return -1;
        } else if (!leftIsSnapshot && rightIsSnapshot) {
            return 1;
        } else {
            return 0;
        }
    }
}
