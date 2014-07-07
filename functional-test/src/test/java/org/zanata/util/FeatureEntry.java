package org.zanata.util;

import java.util.Collections;
import java.util.List;

import org.zanata.feature.Feature;

import com.google.common.primitives.Ints;

/**
 * This object will be converted to JSON.
 * @see org.zanata.util.FeatureInventoryRecorder
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
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

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public int getBugzilla() {
        return bugzilla;
    }

    public void setBugzilla(int bugzilla) {
        this.bugzilla = bugzilla;
    }

    public List<Integer> getTestIds() {
        return testIds;
    }

    public void setTestIds(List<Integer> testIds) {
        this.testIds = testIds;
    }

    public List<Integer> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(List<Integer> planIds) {
        this.planIds = planIds;
    }

    public static enum TestResult {
        Passed, Failed, Ignored
    }
}
