package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SortingType implements Serializable {

    @Getter
    private boolean descending = true;

    @Getter
    private SortOption selectedSortOption = SortOption.ALPHABETICAL;

    @Getter
    private List<SortOption> sortOptions = Lists.newArrayList();

    public SortingType(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public void setSelectedSortOption(SortOption selectedSortOption) {
        if (this.selectedSortOption.equals(selectedSortOption)) {
            descending = !descending;
        }
        this.selectedSortOption = selectedSortOption;
    }

    public enum SortOption {
        PERCENTAGE("percent translated"), HOURS("hours remaining"), WORDS(
            "words remaining"), ALPHABETICAL("alphabetical"), LAST_UPDATED(
            "last updated"), LAST_TRANSLATED("last translated");

        @Getter
        String display;

        SortOption(String display) {
            this.display = display;
        }
    }
}
