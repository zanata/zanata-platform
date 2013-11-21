/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zanata.common.LocaleId;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.ui.model.statistic.WordStatistic;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface VersionGroupService {

    /**
     * Return all active groups + maintained groups of the person
     *
     * @param person
     */
    List<HIterationGroup> getAllActiveAndMaintainedGroups(HPerson person);

    /**
     * Return all active groups
     *
     */
    List<HIterationGroup> getAllActiveGroups();

    /**
     * Search project version by fuzzy matching of version slug or project slug
     *
     * @param searchTerm
     */
    List<HProjectIteration> searchLikeSlugOrProjectSlug(String searchTerm);

    /**
     * Return maintainers of the group
     *
     * @param slug
     */
    List<HPerson> getMaintainersBySlug(String slug);

    /**
     * Return if a version had joined a group
     *
     * @param groupSlug
     * @param projectIterationId
     */
    boolean isVersionInGroup(String groupSlug, Long projectIterationId);

    /**
     * Return map of statistics for all version and active locales in group
     *
     * @param groupSlug
     * @param localeId
     */
    Map<VersionLocaleKey, WordStatistic> getLocaleStatistic(String groupSlug,
            LocaleId localeId);

    /**
     * Return total message count of the versions in group
     *
     * @param groupSlug
     */
    int getTotalMessageCount(String groupSlug);

    /**
     * Return versions that doesn't contained all active locales of the group.
     *
     * @param groupSlug
     */
    Map<LocaleId, List<HProjectIteration>> getMissingLocaleVersionMap(
            String groupSlug);

    /**
     * Return group's activate locales
     *
     * @param groupSlug
     */
    Set<HLocale> getGroupActiveLocales(String groupSlug);
}
