package org.zanata.ui.model.statistic;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class WordStatistic extends AbstractStatistic {

    @Getter
    @Setter
    private double remainingHours;

    public WordStatistic() {
        super();
    }

    public WordStatistic(int approved, int needReview, int untranslated,
            int translated, int rejected) {
        super(approved, needReview, untranslated, translated, rejected);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("\ntranslated-" + getTranslated());
        sb.append("\nneedReview-" + getNeedReview());
        sb.append("\nuntranslated-" + getUntranslated());
        sb.append("\ntotal-" + getTotal());
        sb.append("\nremainingHours-" + getRemainingHours());

        return sb.toString();
    }
}
