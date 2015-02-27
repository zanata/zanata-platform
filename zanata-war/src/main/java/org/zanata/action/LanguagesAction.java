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
package org.zanata.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HLocale;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;
import org.zanata.ui.InMemoryListFilter;

import com.google.common.collect.Lists;
import lombok.Getter;

@Name("languagesAction")
@Scope(ScopeType.PAGE)
public class LanguagesAction extends InMemoryListFilter<HLocale> implements
        Serializable {
    private static final long serialVersionUID = 1L;
    @In
    private LocaleService localeServiceImpl;

    @In
    private LanguageTeamService languageTeamServiceImpl;

    @In
    private ZanataIdentity identity;

    private List<HLocale> allLanguages;

    @Getter
    private SortingType LanguageSortingList = new SortingType(
            Lists.newArrayList(SortingType.SortOption.ALPHABETICAL,
                    SortingType.SortOption.LOCALE_ID,
                    SortingType.SortOption.MEMBERS));

    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());

    public boolean isUserTeamMember(HLocale locale) {
        if(identity != null) {
            return languageTeamServiceImpl
                    .getLanguageMemberships(identity.getAccountUsername()).contains(locale);
        }
        return false;

    }

    /**
     * Sort language list
     */
    public void sortLanguageList() {
        Collections.sort(allLanguages, languageComparator);
        this.reset();
    }

    @Override
    protected List<HLocale> fetchAll() {
        if (allLanguages == null) {
            if(identity != null
                    && identity.hasRole("admin")) {
                allLanguages = localeServiceImpl.getAllLocales();
            } else {
                allLanguages = localeServiceImpl.getSupportedLocales();
            }
        }
        return allLanguages;
    }

    @Override
    protected boolean include(HLocale elem, String filter) {
        return StringUtils.containsIgnoreCase(elem.retrieveDisplayName(),
                filter)
                || StringUtils.containsIgnoreCase(elem.getLocaleId().getId(),
                        filter);
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
                return o1.getMembers().size() - o2.getMembers().size();
            }
        }
    }
}
