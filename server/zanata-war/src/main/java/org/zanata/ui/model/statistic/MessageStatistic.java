package org.zanata.ui.model.statistic;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MessageStatistic extends AbstractStatistic {

    private static final long serialVersionUID = 1L;

    public MessageStatistic() {
        super();
    }

    public MessageStatistic(int approved, int needReview, int untranslated,
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

        return sb.toString();
    }
}
