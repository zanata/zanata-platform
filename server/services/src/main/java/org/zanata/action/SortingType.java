package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SortingType implements Serializable {
    private static final long serialVersionUID = -5844701953054739028L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private SortOption selectedSortOption = SortOption.ALPHABETICAL;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
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

    public static final class SortOption {
        public final static SortOption PERCENTAGE = new SortOption("Percent translated", false);
        public final static SortOption HOURS = new SortOption("Hours remaining", false);
        public final static SortOption WORDS = new SortOption("Words remaining", false);
        public final static SortOption ALPHABETICAL = new SortOption("Alphabetical", true);
        public final static SortOption LAST_ACTIVITY = new SortOption("Last activity", false);
        public final static SortOption LAST_SOURCE_UPDATE = new SortOption("Last source updated", false);
        public final static SortOption LAST_TRANSLATED = new SortOption("Last translated", false);
        public final static SortOption LAST_UPDATED_BY_YOU = new SortOption("Last updated by you", false);
        public final static SortOption Entry = new SortOption("Entry", false);
        public final static SortOption LOCALE_ID = new SortOption("Locale code", true);
        public final static SortOption MEMBERS = new SortOption("Members", true);
        public final static SortOption CREATED_DATE = new SortOption("Created date", true);
        public final static SortOption NAME = new SortOption("Name", true);
        public final static SortOption ROLE = new SortOption("Role", true);

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
