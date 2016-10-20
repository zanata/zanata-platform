package org.zanata.common;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseTranslationCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private int approved;
    private int needReview;
    private int untranslated;
    private int translated;
    private int rejected;

    public BaseTranslationCount() {
    }

    public BaseTranslationCount(int approved, int needReview,
            int untranslated, int translated, int rejected) {
        this.approved = approved;
        this.needReview = needReview;
        this.untranslated = untranslated;
        this.translated = translated;
        this.rejected = rejected;
    }

    protected BaseTranslationCount(int approved, int needReview,
            int untranslated) {
        this(approved, needReview, untranslated, 0, 0);
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

    public void add(BaseTranslationCount other) {
        this.approved += other.approved;
        this.needReview += other.needReview;
        this.untranslated += other.untranslated;
        this.translated += other.translated;
        this.rejected += other.rejected;
    }

    protected void set(BaseTranslationCount other) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof BaseTranslationCount) {
            BaseTranslationCount o = (BaseTranslationCount) obj;
            return (approved == o.approved && needReview == o.needReview
                    && untranslated == o.untranslated
                    && translated == o.translated && rejected == o.rejected);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = approved;
        result = 31 * result + needReview;
        result = 31 * result + untranslated;
        result = 31 * result + translated;
        result = 31 * result + rejected;
        return result;
    }
}
