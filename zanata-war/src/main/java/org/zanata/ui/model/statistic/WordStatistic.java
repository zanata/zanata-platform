package org.zanata.ui.model.statistic;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 * TODO: make this class immutable
 */
public class WordStatistic extends AbstractStatistic {

    private static final long serialVersionUID = -8807499518683834883L;

    private double remainingHours;

    public WordStatistic() {
        super();
    }

    public WordStatistic(int approved, int needReview, int untranslated,
            int translated, int rejected) {
        super(approved, needReview, untranslated, translated, rejected);
    }

    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("\ntranslated-" + getTranslated());
        sb.append("\nneedReview-" + getNeedReview());
        sb.append("\nuntranslated-" + getUntranslated());
        sb.append("\ntotal-" + getTotal());
        sb.append("\nremainingHours-" + getRemainingHours());

        return sb.toString();
    }

    public synchronized double getRemainingHours() {
        return remainingHours;
    }

    public synchronized void setRemainingHours(double remainingHours) {
        this.remainingHours = remainingHours;
    }
}
