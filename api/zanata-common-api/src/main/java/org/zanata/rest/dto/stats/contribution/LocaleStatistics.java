package org.zanata.rest.dto.stats.contribution;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "locale", "translation-stats", "review-stats" })
public class LocaleStatistics implements Serializable {
    public LocaleStatistics() {
    }

    public LocaleStatistics(LocaleId locale) {
        this(locale, null, null);
    }

    public LocaleStatistics(LocaleId locale,
            BaseContributionStatistic translationStats,
            BaseContributionStatistic reviewStats) {
        this.locale = locale;
        this.translationStats = translationStats;
        this.reviewStats = reviewStats;
    }

    private LocaleId locale;

    private BaseContributionStatistic translationStats;

    private BaseContributionStatistic reviewStats;

    @JsonProperty("locale")
    public LocaleId getLocale() {
        return locale;
    }

    @JsonProperty("translation-stats")
    public BaseContributionStatistic getTranslationStats() {
        return translationStats;
    }

    @JsonProperty("review-stats")
    public BaseContributionStatistic getReviewStats() {
        return reviewStats;
    }

    public void setTranslationStats(
            BaseContributionStatistic translationStats) {
        this.translationStats = translationStats;
    }

    public void setReviewStats(
            BaseContributionStatistic reviewStats) {
        this.reviewStats = reviewStats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocaleStatistics))
            return false;

        LocaleStatistics that = (LocaleStatistics) o;

        if (locale != null ? !locale.equals(that.locale) : that.locale != null)
            return false;
        if (translationStats != null
                ? !translationStats.equals(that.translationStats)
                : that.translationStats != null)
            return false;
        return !(reviewStats != null ? !reviewStats.equals(that.reviewStats)
                : that.reviewStats != null);

    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result +
                (translationStats != null ? translationStats.hashCode() : 0);
        result =
                31 * result
                        + (reviewStats != null ? reviewStats.hashCode() : 0);
        return result;
    }
}
