package org.zanata.ui.model.statistic;

import org.zanata.common.ContentState;

import java.io.Serializable;

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

    public synchronized void increment(ContentState state, int count) {
        set(state, get(state) + count);
    }

    public synchronized void decrement(ContentState state, int count) {
        set(state, get(state) - count);
    }

    public synchronized void set(ContentState state, int value) {
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

    public synchronized int get(ContentState state) {
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

    public synchronized void add(AbstractStatistic other) {
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

    public synchronized int getTotal() {
        return approved + needReview + untranslated + translated + rejected;
    }

    public synchronized int getApproved() {
        return approved;
    }

    public synchronized int getNeedReview() {
        return needReview;
    }

    public synchronized int getUntranslated() {
        return untranslated;
    }

    public synchronized int getTranslated() {
        return translated + approved;
    }

    public synchronized int getRejected() {
        return rejected;
    }

    public synchronized double getPercentage(ContentState contentState) {
        switch (contentState) {
            case Translated:
                return getPercentTranslated();
            case NeedReview:
                return getPercentFuzzy();
            case New:
                return getPercentUntranslated();
            case Approved:
                return getPercentApproved();
            case Rejected:
                return getPercentRejected();
            default:
                throw new RuntimeException("not implemented for state "
                    + contentState.name());
        }
    }

    public synchronized double getPercentTranslated() {
        return getPercentage(getTranslated());
    }

    public synchronized double getPercentFuzzy() {
        return getPercentage(getNeedReview());
    }

    public synchronized double getPercentRejected() {
        return getPercentage(getRejected());
    }

    public synchronized double getPercentApproved() {
        return getPercentage(getApproved());
    }

    public synchronized double getPercentUntranslated() {
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
    public synchronized boolean equals(Object obj) {
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
