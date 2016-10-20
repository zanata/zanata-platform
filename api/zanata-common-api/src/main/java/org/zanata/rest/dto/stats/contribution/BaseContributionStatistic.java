package org.zanata.rest.dto.stats.contribution;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "approved", "rejected", "translated", "needReview" })
public class BaseContributionStatistic implements Serializable {

    /**
     * Use Wrapper type (Integer) instead of primitive (int) in properties to
     * prevent displaying of irrelevant review stats field in
     * {@link LocaleStatistics#reviewStats}
     *
     * {@link LocaleStatistics#translationStats}: all fields
     * {@link LocaleStatistics#reviewStats}: approved and rejected
     */

    @Nullable
    private Integer approved;

    @Nullable
    private Integer needReview;

    @Nullable
    private Integer translated;

    @Nullable
    private Integer rejected;

    public BaseContributionStatistic() {
    }

    public BaseContributionStatistic(Integer approved, Integer needReview,
        Integer translated, Integer rejected) {
        this.approved = approved;
        this.needReview = needReview;
        this.translated = translated;
        this.rejected = rejected;
    }

    public void set(ContentState state, Integer value) {
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

    @JsonProperty("approved")
    public Integer getApproved() {
        return approved;
    }

    @JsonProperty("needReview")
    public Integer getNeedReview() {
        return needReview;
    }

    @JsonProperty("translated")
    public Integer getTranslated() {
        return translated;
    }

    @JsonProperty("rejected")
    public Integer getRejected() {
        return rejected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseContributionStatistic)) return false;

        BaseContributionStatistic that = (BaseContributionStatistic) o;

        if (approved != null ? !approved.equals(that.approved) :
            that.approved != null) return false;
        if (needReview != null ? !needReview.equals(that.needReview) :
            that.needReview != null) return false;
        if (translated != null ? !translated.equals(that.translated) :
            that.translated != null) return false;
        return !(rejected != null ? !rejected.equals(that.rejected) :
            that.rejected != null);

    }

    @Override
    public int hashCode() {
        int result = approved != null ? approved.hashCode() : 0;
        result = 31 * result + (needReview != null ? needReview.hashCode() : 0);
        result = 31 * result + (translated != null ? translated.hashCode() : 0);
        result = 31 * result + (rejected != null ? rejected.hashCode() : 0);
        return result;
    }
}
