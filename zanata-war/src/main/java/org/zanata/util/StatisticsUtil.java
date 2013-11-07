package org.zanata.util;

import org.zanata.common.AbstractTranslationCount;
import org.zanata.common.ContentState;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StatisticsUtil {

    public static int calculateUntranslated(Long totalCount,
            AbstractTranslationCount translationCount) {
        return totalCount.intValue()
                - translationCount.get(ContentState.Translated)
                - translationCount.get(ContentState.NeedReview)
                - translationCount.get(ContentState.Approved)
                - translationCount.get(ContentState.Rejected);
    }

    public static String formatPercentage(double percentage) {
        return String.valueOf(Math.floor(percentage));
    }

    public static String formatHours(double hours) {
        return String.valueOf(Math.ceil(hours * 100.0) / 100);
    }
}
