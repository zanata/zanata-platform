/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.zanata.feature.Feature;

import com.google.common.primitives.Ints;

/**
 * This object will be converted to JSON.
 * @see org.zanata.util.FeatureInventoryRecorder
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class FeatureEntry {
    private String testName;
    private String category;
    private String summary;
    private String testResult;
    private int bugzilla;
    private List<Integer> testIds;
    private List<Integer> planIds;

    @SuppressWarnings("unused")
    FeatureEntry() {
        // used by JSON serializer/deserializer
    }

    public FeatureEntry(Feature feature, String displayName,
            TestResult result) {
        testName = displayName;
        category = getLastPackageName(displayName);
        summary = feature.summary();
        bugzilla = feature.bugzilla();
        testIds = toList(feature.tcmsTestCaseIds());
        planIds = toList(feature.tcmsTestPlanIds());
        testResult = result.name();
    }

    private static List<Integer> toList(int[] ints) {
        if (ints == null) {
            return Collections.emptyList();
        }
        return Ints.asList(ints);
    }

    /**
     * quick and dirty way to generate category. Currently it assumes last
     * package name is category. i.e. if the test name is
     * changePasswordAreOfRequiredLength
     * (org.zanata.feature.account.ChangePasswordTest), the category will be
     * considered as account.
     *
     * @param testDisplayName
     *            JUnit test display name
     * @return category name
     */
    private static String getLastPackageName(String testDisplayName) {
        try {
            int lastDot = testDisplayName.lastIndexOf(".");
            String strippedClassName = testDisplayName.substring(0, lastDot);
            int secondLastDot = strippedClassName.lastIndexOf(".");
            String lastPackageName = strippedClassName
                    .substring(secondLastDot + 1, strippedClassName.length());
            return lastPackageName;
        } catch (Exception e) {
            // in case something goes wrong
            return "None";
        }
    }

    public static enum TestResult {
        Passed, Failed, Ignored
    }
}
