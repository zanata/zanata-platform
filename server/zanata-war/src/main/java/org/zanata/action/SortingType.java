package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SortingType implements Serializable {
    private SortOption selectedSortOption = SortOption.ALPHABETICAL;
    private List<SortOption> sortOptions = Lists.newArrayList();

    public SortingType(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public SortingType(List<SortOption> sortOptions, SortOption defaultSort) {
        this.sortOptions = sortOptions;
        this.selectedSortOption = defaultSort;
    }

    public void setSelectedSortOption(SortOption selectedSortOption) {
        if (this.selectedSortOption.equals(selectedSortOption)) {
            selectedSortOption.setAscending(!selectedSortOption.isAscending());
        }
        this.selectedSortOption = selectedSortOption;
    }

    public enum SortOption {
        PERCENTAGE("Percent translated", false),
        HOURS("Hours remaining", false),
        WORDS("Words remaining", false),
        ALPHABETICAL("Alphabetical", true),
        LAST_ACTIVITY("Last activity", false),
        LAST_SOURCE_UPDATE("Last source updated", false),
        LAST_TRANSLATED("Last translated", false),
        LAST_UPDATED_BY_YOU("Last updated by you", false),
        Entry("Entry", false),
        LOCALE_ID("Locale code", true),
        MEMBERS("Members", true),
        CREATED_DATE("Created date", true),
        NAME("Name", true),
        ROLE("Role", true);
        String display;
        boolean ascending; // default sort

        SortOption(String display, boolean ascending) {
            this.display = display;
            this.ascending = ascending;
        }

        public String getDisplay() {
            return this.display;
        }

        public boolean isAscending() {
            return this.ascending;
        }

        public void setAscending(final boolean ascending) {
            this.ascending = ascending;
        }
    }

    public SortOption getSelectedSortOption() {
        return this.selectedSortOption;
    }

    public List<SortOption> getSortOptions() {
        return this.sortOptions;
    }
}
