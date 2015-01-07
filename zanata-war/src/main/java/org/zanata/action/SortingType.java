package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SortingType implements Serializable {

    @Getter
    private SortOption selectedSortOption = SortOption.ALPHABETICAL;

    @Getter
    private List<SortOption> sortOptions = Lists.newArrayList();

    public SortingType(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public void setSelectedSortOption(SortOption selectedSortOption) {
        if (this.selectedSortOption.equals(selectedSortOption)) {
            selectedSortOption.setAscending(!selectedSortOption.isAscending());
        }
        this.selectedSortOption = selectedSortOption;
    }

    public enum SortOption {
        PERCENTAGE("Percent translated", false),
        HOURS("Hours remaining", false), WORDS("Words remaining", false),
        ALPHABETICAL("Alphabetical", true), LAST_ACTIVITY("Last activity",
                false), LAST_SOURCE_UPDATE("Last source updated", false),
        LAST_TRANSLATED("Last translated", false), LAST_UPDATED_BY_YOU(
                "Last updated by you", false), Entry("Entry", false),
        LOCALE_ID("Locale code", true), MEMBERS("Members", true),
        CREATED_DATE("Created date", true);

        @Getter
        String display;

        @Getter
        @Setter
        boolean ascending; // default sort

        SortOption(String display, boolean ascending) {
            this.display = display;
            this.ascending = ascending;
        }
    }
}
