package org.zanata.webtrans.client.ui;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;

public interface HasTranslationStats {
    public static enum LabelFormat {
        PERCENT_COMPLETE, PERCENT_COMPLETE_HRS;
    }

    void setStats(ContainerTranslationStatistics stats, boolean statsByWords);
}
