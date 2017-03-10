/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.ui;

import java.util.Date;
import org.zanata.action.SortingType;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.DateUtil;
import org.zanata.util.StatisticsUtil;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 *         Action Handler for sort component - sortlist.xhtml
 */
public abstract class AbstractSortAction {

    // reset all page cached statistics

    public abstract void resetPageData();

    protected abstract void loadStatistics();

    protected abstract String getMessage(String key, Object... args);

    private final SortingType languageSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE,
                    SortingType.SortOption.WORDS));

    protected DisplayUnit getDisplayUnit(SortingType.SortOption sortOption,
            WordStatistic statistic, Date date) {
        if (statistic.getTotal() == 0) {
            return new DisplayUnit("stats--mini",
                    getMessage("jsf.document.noContent.label"), "",
                    getMessage("jsf.document.noContent.title"));
        }
        DisplayUnit displayUnit;
        String displayString = null;
        switch (sortOption) {
        case HOURS:
            displayUnit = new DisplayUnit("stats--small",
                    StatisticsUtil.formatHours(statistic.getRemainingHours()),
                    getMessage("jsf.stats.HoursRemaining"), "");
            break;

        case WORDS:
            displayUnit = new DisplayUnit("stats--small",
                    String.valueOf(statistic.getUntranslated()),
                    getMessage("jsf.WordsRemaining"), "");
            break;

        case LAST_ACTIVITY:
            displayUnit = new DisplayUnit("stats--mini",
                    DateUtil.getHowLongAgoDescription(date),
                    getMessage("jsf.LastUpdated"),
                    DateUtil.formatShortDate(date));
            break;

        case LAST_UPDATED_BY_YOU:
            displayString = date == null ? "never"
                    : DateUtil.getHowLongAgoDescription(date);
            displayUnit = new DisplayUnit("stats--mini", displayString,
                    getMessage("jsf.LastUpdatedByYou"),
                    DateUtil.formatShortDate(date));
            break;

        case LAST_TRANSLATED:
            displayString = date == null ? "never"
                    : DateUtil.getHowLongAgoDescription(date);
            displayUnit = new DisplayUnit("stats--mini", displayString,
                    getMessage("jsf.LastTranslated"),
                    DateUtil.formatShortDate(date));
            break;

        case LAST_SOURCE_UPDATE:
            displayString = date == null ? "never"
                    : DateUtil.getHowLongAgoDescription(date);
            displayUnit = new DisplayUnit("stats--mini", displayString,
                    getMessage("jsf.LastUpdated"),
                    DateUtil.formatShortDate(date));
            break;

        default:
            String figure = StatisticsUtil
                    .formatPercentage(statistic.getPercentTranslated()) + "%";
            String style = statistic.getPercentTranslated() == 0
                    ? "stats--small txt--neutral" : "stats--small txt--success";
            displayUnit = new DisplayUnit(style, figure,
                    getMessage("jsf.Translated"), "");
            break;

        }
        return displayUnit;
    }

    public final class DisplayUnit {
        private String cssClass;
        private String figure;
        private String unit;
        private String title;

        public String getCssClass() {
            return this.cssClass;
        }

        public String getFigure() {
            return this.figure;
        }

        public String getUnit() {
            return this.unit;
        }

        public String getTitle() {
            return this.title;
        }

        @java.beans.ConstructorProperties({ "cssClass", "figure", "unit",
                "title" })
        public DisplayUnit(final String cssClass, final String figure,
                final String unit, final String title) {
            this.cssClass = cssClass;
            this.figure = figure;
            this.unit = unit;
            this.title = title;
        }
    }

    public int compareWordStatistic(WordStatistic stats1, WordStatistic stats2,
            SortingType.SortOption sortOption) {
        if (stats1 == null) {
            return -1;
        } else if (stats2 == null) {
            return 1;
        }
        switch (sortOption) {
        case HOURS:
            return Double.compare(stats1.getRemainingHours(),
                    stats2.getRemainingHours());

        case PERCENTAGE:
            return Double.compare(stats1.getPercentTranslated(),
                    stats2.getPercentTranslated());

        case WORDS:
            return Double.compare(stats1.getUntranslated(),
                    stats2.getUntranslated());

        default:
            return 0;

        }
    }

    public SortingType getLanguageSortingList() {
        return this.languageSortingList;
    }
}
