/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("versionGroupServiceImpl")
@Scope(ScopeType.STATELESS)
public class VersionGroupServiceImpl implements VersionGroupService {

    @In
    private VersionGroupDAO versionGroupDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private VersionStateCache versionStateCacheImpl;

    @In
    private LocaleService localeServiceImpl;

    @Override
    public Map<VersionLocaleKey, WordStatistic> getLocaleStatistic(
            String groupSlug, LocaleId localeId) {

        Map<VersionLocaleKey, WordStatistic> statisticMap = Maps.newHashMap();
        for (HProjectIteration version : getNonObsoleteProjectIterationsBySlug(groupSlug)) {
            if (version.getStatus() != EntityStatus.OBSOLETE) {
                WordStatistic statistic =
                        versionStateCacheImpl.getVersionStatistics(
                                version.getId(), localeId);
                statistic.setRemainingHours(StatisticsUtil
                        .getRemainingHours(statistic));

                statisticMap.put(
                        new VersionLocaleKey(version.getId(), localeId),
                        statistic);
            }
        }

        return statisticMap;
    }

    private List<HProjectIteration> getNotObsoleteProjectIterations(
            HIterationGroup group) {
        return projectIterationDAO.getByGroup(group);
    }

    @Override
    public int getTotalMessageCount(String groupSlug) {
        int result = 0;
        HIterationGroup group = versionGroupDAO.getBySlug(groupSlug);

        for (HProjectIteration version : getNotObsoleteProjectIterations(group)) {
            result +=
                    projectIterationDAO.getTotalMessageCountForIteration(
                            version.getId()).intValue();
        }
        result = result * group.getActiveLocales().size();
        return result;
    }

    @Override
    public List<HIterationGroup>
            getAllActiveAndMaintainedGroups(HPerson person) {
        List<HIterationGroup> activeVersions = getAllActiveGroups();
        if (person == null) {
            return activeVersions;
        }

        List<HIterationGroup> obsoleteVersions =
                versionGroupDAO.getAllObsoleteVersionGroups();

        List<HIterationGroup> filteredList = Lists.newArrayList();
        for (HIterationGroup obsoleteGroup : obsoleteVersions) {
            if (obsoleteGroup.getMaintainers().contains(person)) {
                filteredList.add(obsoleteGroup);
            }
        }
        activeVersions.addAll(filteredList);

        return activeVersions;
    }

    @Override
    public List<HIterationGroup> getAllActiveGroups() {
        return versionGroupDAO.getAllActiveVersionGroups();
    }

    @Override
    public List<HProjectIteration>
            searchLikeSlugOrProjectSlug(String searchTerm) {
        return projectIterationDAO.searchLikeSlugOrProjectSlug(searchTerm);
    }

    @Override
    public List<HPerson> getMaintainersBySlug(String groupSlug) {
        return versionGroupDAO.getMaintainersBySlug(groupSlug);
    }

    @Override
    public boolean isVersionInGroup(String groupSlug, Long projectIterationId) {
        HIterationGroup group = versionGroupDAO.getBySlug(groupSlug);
        if (group != null && projectIterationId != null) {
            for (HProjectIteration iteration : group.getProjectIterations()) {
                if (iteration.getId().equals(projectIterationId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Set<HLocale> getGroupActiveLocales(String groupSlug) {
        HIterationGroup group = versionGroupDAO.getBySlug(groupSlug);
        if (group != null) {
            return group.getActiveLocales();
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public List<HProjectIteration> getNonObsoleteProjectIterationsBySlug(
            String groupSlug) {
        HIterationGroup group = versionGroupDAO.getBySlug(groupSlug);
        return getNotObsoleteProjectIterations(group);
    }

    /**
     * Return versions that don't contained all active locales of the group.
     *
     * @param groupSlug
     */
    @Override
    public Map<LocaleId, List<HProjectIteration>> getMissingLocaleVersionMap(
            String groupSlug) {
        Map<LocaleId, List<HProjectIteration>> result = Maps.newHashMap();

        HIterationGroup group = versionGroupDAO.getBySlug(groupSlug);
        if (group != null) {
            for (HLocale activeLocale : group.getActiveLocales()) {
                List<HProjectIteration> versionList = Lists.newArrayList();
                for (HProjectIteration version : getNotObsoleteProjectIterations(group)) {
                    if (!isLocaleActivatedInVersion(version, activeLocale)) {
                        versionList.add(version);
                    }
                }
                result.put(activeLocale.getLocaleId(), versionList);
            }
        }
        return result;
    }

    /**
     * Return if the locale enabled by default in server and is activate in the
     * version. Return true if version and project doesn't overrides locale.
     *
     * Fallback to project customised locale if version doesn't overrides
     * locales
     *
     * @param version
     * @param locale
     */
    private boolean isLocaleActivatedInVersion(HProjectIteration version,
            HLocale locale) {
        List<HLocale> versionLocales =
                localeServiceImpl.getSupportedLanguageByProjectIteration(
                        version.getProject().getSlug(), version.getSlug());
        return versionLocales.contains(locale);

    }
}
