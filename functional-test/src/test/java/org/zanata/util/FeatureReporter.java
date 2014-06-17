package org.zanata.util;

import org.zanata.feature.Feature;

public interface FeatureReporter {
    String toReportEntry(Feature feature, String displayName, TestResult testResult);
    enum TestResult {
        Passed, Failed, Ignored
    }
}
