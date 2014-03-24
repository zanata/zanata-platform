/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.ui;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.zanata.action.ProjectHomeAction;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class FilterUtil {
    public static List<HLocale> filterLanguageList(final String query,
            final List<HLocale> unfiltered) {
        if (StringUtils.isEmpty(query)) {
            return unfiltered;
        }
        Collection<HLocale> filtered =
                Collections2.filter(unfiltered, new Predicate<HLocale>() {
                    @Override
                    public boolean apply(@Nullable HLocale input) {
                        return StringUtils.startsWithIgnoreCase(input
                                .getLocaleId().getId(), query)
                                || StringUtils.containsIgnoreCase(
                                        input.retrieveDisplayName(), query);
                    }
                });

        return Lists.newArrayList(filtered);
    }

    /**
     * Return filtered list of HPerson from personList which are NOT in
     * allPersonList
     *
     * @param allPersonList
     * @param personList
     * @return
     */
    public static List<HPerson> filterOutPersonList(
            final List<HPerson> allPersonList, List<HPerson> personList) {
        Collection<HPerson> filtered =
                Collections2.filter(personList, new Predicate<HPerson>() {
                    @Override
                    public boolean apply(@Nullable HPerson input) {
                        return !allPersonList.contains(input);
                    }
                });

        return Lists.newArrayList(filtered);
    }

    public static List<HPerson> filterPersonList(final String query,
            final List<HPerson> unfiltered) {
        if (StringUtils.isEmpty(query)) {
            return unfiltered;
        }
        Collection<HPerson> filtered =
                Collections2.filter(unfiltered, new Predicate<HPerson>() {
                    @Override
                    public boolean apply(@Nullable HPerson input) {
                        return StringUtils.containsIgnoreCase(input.getName(),
                                query);
                    }
                });

        return Lists.newArrayList(filtered);
    }

    public static List<HIterationGroup> filterGroupList(final String query,
            final List<HIterationGroup> unfiltered) {
        if (StringUtils.isEmpty(query)) {
            return unfiltered;
        }
        Collection<HIterationGroup> filtered =
                Collections2.filter(unfiltered,
                        new Predicate<HIterationGroup>() {
                            @Override
                            public boolean
                                    apply(@Nullable HIterationGroup input) {
                                return StringUtils.containsIgnoreCase(
                                        input.getName(), query)
                                        || StringUtils.containsIgnoreCase(
                                                input.getSlug(), query);
                            }
                        });

        return Lists.newArrayList(filtered);
    }

    public static List<HDocument> filterDocumentList(final String query,
            final List<HDocument> unfiltered) {
        if (StringUtils.isEmpty(query)) {
            return unfiltered;
        }
        Collection<HDocument> filtered =
                Collections2.filter(unfiltered, new Predicate<HDocument>() {
                    @Override
                    public boolean apply(@Nullable HDocument input) {
                        return StringUtils.containsIgnoreCase(input.getName(),
                                query)
                                || StringUtils.containsIgnoreCase(
                                        input.getPath(), query);
                    }
                });

        return Lists.newArrayList(filtered);
    }

    public static List<HProjectIteration> filterVersionListWithProjectName(
            final String query, final List<HProjectIteration> unfiltered) {
        if (StringUtils.isEmpty(query)) {
            return unfiltered;
        }
        Collection<HProjectIteration> filtered =
                Collections2.filter(unfiltered,
                        new Predicate<HProjectIteration>() {
                            @Override
                            public boolean apply(
                                    @Nullable HProjectIteration input) {
                                HProject project = input.getProject();
                                return StringUtils.containsIgnoreCase(
                                        project.getName(), query);
                            }
                        });

        return Lists.newArrayList(filtered);
    }

    public static List<ProjectHomeAction.VersionItem> filterVersionItemList(
            final String query,
            final List<ProjectHomeAction.VersionItem> unfiltered) {
        if (StringUtils.isEmpty(query)) {
            return unfiltered;
        }
        Collection<ProjectHomeAction.VersionItem> filtered =
                Collections2.filter(unfiltered,
                        new Predicate<ProjectHomeAction.VersionItem>() {
                            @Override
                            public
                                    boolean
                                    apply(@Nullable ProjectHomeAction.VersionItem input) {
                                input.getVersion().getSlug();
                                return StringUtils.containsIgnoreCase(input
                                        .getVersion().getSlug(), query);
                            }
                        });

        return Lists.newArrayList(filtered);
    }

    /**
     * Return true if
     *
     * 1) Query is empty 2) hLocale is NOT in localeList and hLocale's display
     * name/localeId matches with query.
     *
     *
     *
     * @param localeList
     * @param hLocale
     * @param query
     * @return
     */
    public static boolean isIncludeLocale(Collection<HLocale> localeList,
            HLocale hLocale, String query) {
        return !localeList.contains(hLocale)
                && (StringUtils.startsWithIgnoreCase(hLocale.getLocaleId()
                        .getId(), query) || StringUtils.containsIgnoreCase(
                        hLocale.retrieveDisplayName(), query));
    }
}
