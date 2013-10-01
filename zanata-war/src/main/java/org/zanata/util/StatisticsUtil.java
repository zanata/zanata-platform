package org.zanata.util;

import org.zanata.common.AbstractTranslationCount;
import org.zanata.common.ContentState;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StatisticsUtil {
    private StatisticsUtil() {
    }

    public static int calculateUntranslated(Long totalCount,
            AbstractTranslationCount translationCount) {
        return totalCount.intValue()
                - translationCount.get(ContentState.Translated)
                - translationCount.get(ContentState.NeedReview)
                - translationCount.get(ContentState.Approved)
                - translationCount.get(ContentState.Rejected);
    }
}
