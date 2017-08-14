/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.dto.stats;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.common.BaseTranslationCount;
import org.zanata.common.ContentState;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;

/**
 * Translation statistics. Contains actual numbers and other information about
 * the state of translation.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlType(name = "translationStatistics", propOrder = { "total", "untranslated",
        "needReview", "translated", "approved", "rejected", "translatedOnly",
        "fuzzy", "unit", "locale", "lastTranslated" })
@XmlRootElement(name = "translationStats")
@JsonIgnoreProperties(value = { "percentTranslated", "percentNeedReview",
        "percentUntranslated", "incomplete", "draft" }, ignoreUnknown = true)
@JsonPropertyOrder({ "total", "untranslated", "needReview", "translated",
        "approved", "rejected", "readyForReview", "fuzzy", "unit", "locale",
        "lastTranslated" })
@Label("Translation Statistics")
public class TranslationStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private StatUnit unit;
    private BaseTranslationCount translationCount;
    private String locale;
    private String lastTranslated;

    private @Nullable
    Date lastTranslatedDate;
    private String lastTranslatedBy;

    /**
     * This is for marshalling purpose only.
     */
    public TranslationStatistics() {
        this(StatUnit.MESSAGE);
    }

    public TranslationStatistics(StatUnit statUnit) {
        unit = statUnit;
        if (unit == StatUnit.MESSAGE) {
            translationCount = new TransUnitWords(0, 0, 0);
        } else {
            translationCount = new TransUnitCount(0, 0, 0);
        }
    }

    public TranslationStatistics(TransUnitCount unitCount, String locale) {
        translationCount = unitCount;
        this.unit = StatUnit.MESSAGE;
        this.locale = locale;
    }

    public TranslationStatistics(TransUnitWords wordCount, String locale) {
        translationCount = wordCount;
        this.unit = StatUnit.WORD;
        this.locale = locale;
    }

    /**
     * Calculate remaining hours if StatUnit equals to 'WORD'.
     */
    @XmlTransient
    @JsonIgnore
    public @Nullable Double getRemainingHours() {
        if (unit.equals(StatUnit.WORD)) {
            double untransHours = translationCount.getUntranslated() / 250.0;
            double fuzzyHours = translationCount.getNeedReview() / 500.0;

            return untransHours + fuzzyHours;
        }
        return null;
    }

    /**
     * Number of untranslated elements.
     */
    @XmlAttribute
    @DocumentationExample("25")
    public long getUntranslated() {
        return translationCount.getUntranslated();
    }

    public void setUntranslated(long untranslated) {
        translationCount.set(ContentState.New, (int) untranslated);
    }

    /**
     * Number of elements that need review (i.e. Fuzzy or Rejected).
     */
    @XmlTransient
    @JsonIgnore
    public long getDraft() {
        return translationCount.getNeedReview()
                + translationCount.getRejected();
    }

    /**
     * This is for REST backward compatibility.
     *
     * @return Number of elements that need review (i.e. Fuzzy or Rejected)
     * @deprecated See {@link #getDraft()}
     */
    @XmlAttribute
    @Deprecated
    public long getNeedReview() {
        return getDraft();
    }

    /**
     * This will only return fuzzy translation.
     *
     * @return
     */
    @XmlAttribute
    @DocumentationExample("50")
    public long getFuzzy() {
        return translationCount.getNeedReview();
    }

    public void setFuzzy(long fuzzy) {
        translationCount.set(ContentState.NeedReview, (int) fuzzy);
    }

    /**
     * This is for REST backward compatibility.
     *
     * @return Number of translated and approved elements.
     * @deprecated See {@link #getTranslatedOnly()} and
     *             {@link #getTranslatedAndApproved()}
     */
    @XmlAttribute
    @Deprecated
    public long getTranslated() {
        return getTranslatedAndApproved();
    }

    @XmlTransient
    @JsonIgnore
    public long getTranslatedAndApproved() {
        return translationCount.getTranslated()
                + translationCount.getApproved();
    }

    /**
     * @return number of translated but not yet approved elements.
     */
    @XmlAttribute
    @DocumentationExample("30")
    public long getTranslatedOnly() {
        return translationCount.getTranslated();
    }

    public void setTranslatedOnly(long translatedOnly) {
        translationCount.set(ContentState.Translated, (int) translatedOnly);
    }

    /**
     * @return Number of approved elements.
     */
    @XmlAttribute
    @DocumentationExample("80")
    public long getApproved() {
        return translationCount.getApproved();
    }

    public void setApproved(long approved) {
        translationCount.set(ContentState.Approved, (int) approved);
    }

    @XmlAttribute
    @DocumentationExample("10")
    public long getRejected() {
        return translationCount.getRejected();
    }

    public void setRejected(long rejected) {
        translationCount.set(ContentState.Rejected, (int) rejected);
    }

    /**
     *
     * @return untranslated, fuzzy and rejected count.
     */
    @XmlTransient
    @JsonIgnore
    public long getIncomplete() {
        return translationCount.getUntranslated() + getDraft();
    }

    /**
     * Total number of elements.
     */
    @XmlAttribute
    public long getTotal() {
        return translationCount.getTotal();
    }

    /**
     * Element unit being used to measure the translation counts.
     */
    @XmlAttribute
    public StatUnit getUnit() {
        return unit;
    }

    public void setUnit(StatUnit unit) {
        this.unit = unit;
    }

    /**
     * Locale for the translation statistics.
     */
    @XmlAttribute
    @DocumentationExample("es-ES")
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Last translation information. Includes date and user.
     */
    @XmlAttribute
    @DocumentationExample("31/12/15 23:59 by homer")
    public String getLastTranslated() {
        return lastTranslated;
    }

    public void setLastTranslated(String lastTranslated) {
        this.lastTranslated = lastTranslated;
    }

    @XmlTransient
    @JsonIgnore
    public @Nullable
    Date getLastTranslatedDate() {
        return lastTranslatedDate != null ? new Date(
                lastTranslatedDate.getTime()) : null;
    }

    public void setLastTranslatedDate(@Nullable Date lastTranslatedDate) {
        this.lastTranslatedDate =
                lastTranslatedDate != null ? new Date(
                        lastTranslatedDate.getTime()) : null;
    }

    @XmlTransient
    @JsonIgnore
    public String getLastTranslatedBy() {
        return lastTranslatedBy;
    }

    public void setLastTranslatedBy(String lastTranslatedBy) {
        this.lastTranslatedBy = lastTranslatedBy;
    }

    @XmlTransient
    @JsonIgnore
    public double getPercentTranslated() {
        long total = getTotal();
        if (total <= 0) {
            return 0;
        } else {
            return 100d * getTranslatedAndApproved() / total;
        }
    }

    @XmlTransient
    @JsonIgnore
    public double getPercentDraft() {
        long total = getTotal();
        if (total <= 0) {
            return 0;
        } else {
            return 100d * getDraft() / total;
        }
    }

    @XmlTransient
    @JsonIgnore
    public double getPercentUntranslated() {
        long total = getTotal();
        if (total <= 0) {
            return 0;
        } else {
            return 100d * getUntranslated() / total;
        }
    }

    public void add(TranslationStatistics other) {
        translationCount.add(other.translationCount);
    }

    public void increment(ContentState state, long count) {
        translationCount.increment(state, (int) count);
    }

    public void decrement(ContentState state, long count) {
        translationCount.decrement(state, (int) count);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TranslationStatistics{");
        sb.append("unit=").append(unit);
        sb.append(", translationCount=").append(translationCount);
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", lastTranslated='").append(lastTranslated).append('\'');
        sb.append(", lastTranslatedDate=").append(lastTranslatedDate);
        sb.append(", lastTranslatedBy='").append(lastTranslatedBy).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Label("Stats Unit")
    public enum StatUnit {
        /** Statistics are measured in words. */
        WORD,
        /** Statistics are measured in messages (i.e. entries, text flows) */
        MESSAGE;

    }
}
