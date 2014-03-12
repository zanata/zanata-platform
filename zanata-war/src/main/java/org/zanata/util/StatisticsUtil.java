package org.zanata.util;

import org.zanata.common.AbstractTranslationCount;
import org.zanata.common.ContentState;
import org.zanata.common.TransUnitWords;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.ui.model.statistic.WordStatistic;

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

    public static double getRemainingHours(WordStatistic wordsStatistic) {
        return getRemainingHours(wordsStatistic.getUntranslated(),
                wordsStatistic.getNeedReview() + wordsStatistic.getRejected());
    }

    public static double getRemainingHours(
            TranslationStatistics translationStatistics) {
        return getRemainingHours(translationStatistics.getUntranslated(),
                translationStatistics.getDraft());
    }

    public static double getRemainingHours(TransUnitWords transUnitWords) {
        return getRemainingHours(transUnitWords.getUntranslated(),
                transUnitWords.getNeedReview());
    }

    public static String formatPercentage(double percentage) {
        return String.valueOf(Math.floor(percentage * 100) / 100);
    }

    public static String formatHours(double hours) {
        return String.valueOf(Math.ceil(hours * 100.0) / 100);
    }

    private static double getRemainingHours(double untranslated, double draft) {
        double untranslatedHours = untranslated / 250.0;
        double draftHours = draft / 500.0;

        return untranslatedHours + draftHours;
    }

}
