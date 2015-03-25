package org.zanata.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;
import org.zanata.ui.InMemoryListFilter;

@Name("supportedLanguageAction")
@Scope(ScopeType.PAGE)
public class SupportedLanguageAction extends InMemoryListFilter<HLocale>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @In
    private LocaleService localeServiceImpl;

    private List<HLocale> supportedLanguages;

    private Map<LocaleId, Integer> membersSize = Maps.newHashMap();

    @Getter
    private SortingType LanguageSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.LOCALE_ID,
                    SortingType.SortOption.MEMBERS));

    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());

    @Override
    protected List<HLocale> fetchAll() {
        if (supportedLanguages == null) {
            supportedLanguages = localeServiceImpl.getSupportedLocales();
            for(HLocale locale: supportedLanguages) {
                membersSize.put(locale.getLocaleId(), locale.getMembers()
                        .size());
            }
        }
        return supportedLanguages;
    }

    public int getMemberSize(LocaleId localeId) {
        Integer size = membersSize.get(localeId);
        return size == null ? 0 : size;
    }

    @Override
    protected boolean include(HLocale elem, String filter) {
        return StringUtils.containsIgnoreCase(elem.retrieveDisplayName(),
            filter)
            || StringUtils.containsIgnoreCase(elem.getLocaleId().getId(),
            filter);
    }

    /**
     * Sort language list
     */
    public void sortLanguageList() {
        Collections.sort(supportedLanguages, languageComparator);
        this.reset();
    }

    // sort by name or locale id
    private class LanguageComparator implements Comparator<HLocale> {
        private SortingType sortingType;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale o1, HLocale o2) {
            SortingType.SortOption selectedSortOption =
                    sortingType.getSelectedSortOption();

            if (!selectedSortOption.isAscending()) {
                HLocale temp = o1;
                o1 = o2;
                o2 = temp;
            }

            if (selectedSortOption.equals(SortingType.SortOption.ALPHABETICAL)) {
                return o1.retrieveDisplayName().compareTo(
                        o2.retrieveDisplayName());
            } else if (selectedSortOption
                    .equals(SortingType.SortOption.LOCALE_ID)) {
                return o1.getLocaleId().getId().compareTo(
                        o2.getLocaleId().getId());
            } else {
                return getMemberSize(o1.getLocaleId()) - getMemberSize(o2.getLocaleId());
            }
        }
    }

}
