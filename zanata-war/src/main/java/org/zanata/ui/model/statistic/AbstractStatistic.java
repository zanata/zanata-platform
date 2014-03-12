package org.zanata.ui.model.statistic;

import java.io.Serializable;

import org.zanata.common.ContentState;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    private int approved;
    private int needReview;
    private int untranslated;
    private int translated;
    private int rejected;

    protected AbstractStatistic() {
    }

    protected AbstractStatistic(int approved, int needReview, int untranslated,
            int translated, int rejected) {
        this.approved = approved;
        this.needReview = needReview;
        this.untranslated = untranslated;
        this.translated = translated;
        this.rejected = rejected;
    }

    public void increment(ContentState state, int count) {
        set(state, get(state) + count);
    }

    public void decrement(ContentState state, int count) {
        set(state, get(state) - count);
    }

    public void set(ContentState state, int value) {
        switch (state) {
        case Translated:
            translated = value;
            break;
        case NeedReview:
            needReview = value;
            break;
        case New:
            untranslated = value;
            break;
        case Approved:
            approved = value;
            break;
        case Rejected:
            rejected = value;
            break;
        default:
            throw new RuntimeException("not implemented for state "
                    + state.name());
        }
    }

    public int get(ContentState state) {
        switch (state) {
        case Translated:
            return translated;
        case NeedReview:
            return needReview;
        case New:
            return untranslated;
        case Approved:
            return approved;
        case Rejected:
            return rejected;
        default:
            throw new RuntimeException("not implemented for state "
                    + state.name());
        }
    }

    public void add(AbstractStatistic other) {
        this.approved += other.approved;
        this.needReview += other.needReview;
        this.untranslated += other.untranslated;
        this.translated += other.translated;
        this.rejected += other.rejected;
    }

    protected void set(AbstractStatistic other) {
        this.approved = other.approved;
        this.needReview = other.needReview;
        this.untranslated = other.untranslated;
        this.translated = other.translated;
        this.rejected = other.rejected;
    }

    public int getTotal() {
        return approved + needReview + untranslated + translated + rejected;
    }

    public int getApproved() {
        return approved;
    }

    public int getNeedReview() {
        return needReview;
    }

    public int getUntranslated() {
        return untranslated;
    }

    public int getTranslated() {
        return translated;
    }

    public int getRejected() {
        return rejected;
    }

    public double getPercentTranslated() {
        return getPercentage(getTranslated());
    }

    public double getPercentFuzzy() {
        return getPercentage(getNeedReview());
    }

    public double getPercentRejected() {
        return getPercentage(getRejected());
    }

    public double getPercentApproved() {
        return getPercentage(getApproved());
    }

    public double getPercentUntranslated() {
        return getPercentage(getUntranslated());
    }

    private double getPercentage(double value) {
        long total = getTotal();
        if (total <= 0) {
            return 0;
        }
        double percent = 100d * value / total;
        return percent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof AbstractStatistic) {
            AbstractStatistic o = (AbstractStatistic) obj;
            return (approved == o.approved && needReview == o.needReview
                    && untranslated == o.untranslated
                    && translated == o.translated && rejected == o.rejected);
        }
        return false;
    }

    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}
