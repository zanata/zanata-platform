package org.zanata.util;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.zanata.feature.Feature;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HTMLFeatureReporter implements FeatureReporter {
    public static final String HTML_FORMAT =
            "[%s]";
    public static final String SEPARATOR = " - ";

    @Override
    public String toReportEntry(Feature feature, String displayName,
            TestResult result) {
        String bugzilaLink =
                new Link("https://bugzilla.redhat.com/show_bug.cgi?id=" +
                        feature.bugzilla(), "rhbz" + feature.bugzilla(),
                        feature.bugzilla()).toString();
        List<String> testCases = testCaseLinks(feature.tcmsTestCaseIds());
        List<String> plans = testPlanLinks(feature.tcmsTestPlanIds());

        List<String> allLinks = Lists.newArrayList();
        allLinks.add(bugzilaLink);
        allLinks.addAll(testCases);
        allLinks.addAll(plans);
        String links = Joiner.on(SEPARATOR).skipNulls().join(allLinks);

        StringBuilder builder =
                new StringBuilder("<li>").append(feature.summary());
        builder.append(SEPARATOR).append(links)
                .append(inBracket(displayName))
                .append(inBracket(result.name())).append("</li>");

        return builder.toString();
    }

    private static String inBracket(String thing) {
        return String.format(HTML_FORMAT, thing);
    }

    private List<String> testPlanLinks(int[] tcmsTestPlanIds) {
        List<String> plans = Lists.newArrayList();
        for (int planId : tcmsTestPlanIds) {
            plans.add(new Link(
                    "https://tcms.engineering.redhat.com/plan/" + planId,
                    "tcms plan " + planId, planId).toString());
        }
        return plans;
    }

    private List<String> testCaseLinks(int[] testCaseIds) {
        List<String> cases = Lists.newArrayList();
        for (int testCaseId : testCaseIds) {
            cases.add(new Link(
                    "https://tcms.engineering.redhat.com/case/" + testCaseId,
                    "tcms case " + testCaseId, testCaseId).toString());
        }
        return cases;
    }

    @RequiredArgsConstructor
    class Link {
        private static final String TEMPLATE = "<a href='%s'>%s</a>";
        private final String href;
        private final String linkText;
        // this represents what builds up this link. i.e. the bugzilla number.
        private final int referencedId;

        /**
         * @return null if referencedId is not meaningful otherwise a link.
         */
        @Override
        public String toString() {
            if (referencedId > 0) {
                return String.format(TEMPLATE, href, linkText);
            }
            return null;
        }
    }
}
