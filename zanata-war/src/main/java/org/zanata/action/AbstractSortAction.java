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

package org.zanata.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 *         Action Handler for sort component - sortlist.xhtml
 */
public abstract class AbstractSortAction {

    // reset all page cached statistics
    abstract void resetPageData();

    abstract protected void loadStatistics();

    abstract String getMessage(String key, Object... args);

    @Getter
    private final SortingType languageSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.HOURS,
                    SortingType.SortOption.PERCENTAGE,
                    SortingType.SortOption.WORDS));

    protected DisplayUnit getDisplayUnit(SortingType.SortOption sortOption,
            WordStatistic statistic) {
        DisplayUnit displayUnit;

        switch (sortOption) {
        case HOURS:
            displayUnit =
                    new DisplayUnit("", StatisticsUtil.formatHours(statistic
                            .getRemainingHours()),
                            getMessage("jsf.stats.HoursRemaining"));
            break;

        case WORDS:
            displayUnit =
                    new DisplayUnit("", String.valueOf(statistic
                            .getUntranslated()), getMessage("jsf.Words"));
            break;

        default:
            String figure =
                    StatisticsUtil.formatPercentage(statistic
                            .getPercentTranslated()) + "%";
            String style =
                    statistic.getPercentTranslated() == 0 ? "txt--neutral"
                            : "txt--success";
            displayUnit =
                    new DisplayUnit(style, figure, getMessage("jsf.Translated"));
            break;
        }
        return displayUnit;
    }

    @Getter
    @AllArgsConstructor
    public final class DisplayUnit {
        private String cssClass;
        private String figure;
        private String unit;
    }

    public int compareWordStatistic(WordStatistic stats1, WordStatistic stats2,
            SortingType.SortOption sortOption) {
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
}
