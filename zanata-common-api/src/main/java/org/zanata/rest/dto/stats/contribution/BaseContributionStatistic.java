package org.zanata.rest.dto.stats.contribution;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.zanata.common.BaseTranslationCount;
import org.zanata.common.ContentState;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseContributionStatistic implements Serializable {

    private int approved;
    private int needReview;
    private int translated;
    private int rejected;

    public BaseContributionStatistic() {
    }

    public BaseContributionStatistic(int approved, int needReview,
            int translated, int rejected) {
        this.approved = approved;
        this.needReview = needReview;
        this.translated = translated;
        this.rejected = rejected;
    }

    public void set(ContentState state, int value) {
        switch (state) {
        case Translated:
            translated = value;
            break;
        case NeedReview:
            needReview = value;
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
        case Approved:
            return approved;
        case Rejected:
            return rejected;
        default:
            throw new RuntimeException("not implemented for state "
                    + state.name());
        }
    }

    public int getApproved() {
        return approved;
    }

    public int getNeedReview() {
        return needReview;
    }

    public int getTranslated() {
        return translated;
    }

    public int getRejected() {
        return rejected;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof BaseContributionStatistic) {
            BaseContributionStatistic o = (BaseContributionStatistic) obj;
            return (getApproved() == o.getApproved()
                    && getNeedReview() == o.getNeedReview()
                    && getTranslated() == o.getTranslated() && getRejected() == o
                        .getRejected());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = getApproved();
        result = 31 * result + getNeedReview();
        result = 31 * result + getTranslated();
        result = 31 * result + getRejected();
        return result;
    }
}
