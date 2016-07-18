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

package org.zanata.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.jpa.FullText;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionLocaleKey;
import org.zanata.service.impl.VersionStateCacheImpl.VersionStatisticLoader;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Zanata;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
@AdditionalClasses({
        VersionStateCacheImpl.class,
        LocaleServiceImpl.class,
        VersionStatisticLoader.class
})
public class VersionGroupServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    private VersionGroupServiceImpl versionGroupServiceImpl;

    private final String GROUP1_SLUG = "group1";
    private final String GROUP2_SLUG = "group2";
    private final String GROUP3_SLUG = "group3";

    @Produces @Zanata CacheContainer cacheContainer =
            new InfinispanTestCacheContainer();
    @Produces @FullText @Mock FullTextEntityManager fullTextEntityManager;
    @Produces IServiceLocator serviceLocator = ServiceLocator.instance();

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/GroupsTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @InRequestScope
    public void getLocaleStatisticTest1() {
        LocaleId localeId = LocaleId.DE;

        Map<VersionLocaleKey, WordStatistic> result =
                versionGroupServiceImpl.getLocaleStatistic(GROUP1_SLUG,
                        localeId);

        // 3 versions in group1
        assertThat(result.size(), equalTo(3));
    }

    @Test
    @InRequestScope
    public void getLocaleStatisticTest2() {
        LocaleId localeId = LocaleId.DE;

        Map<VersionLocaleKey, WordStatistic> result =
                versionGroupServiceImpl.getLocaleStatistic(GROUP2_SLUG,
                        localeId);

        // 2 versions in group1
        assertThat(result.size(), equalTo(2));
    }

    @Test
    @InRequestScope
    public void getTotalMessageCountTest1() {
        int totalMessageCount =
                versionGroupServiceImpl.getTotalMessageCount(GROUP1_SLUG);
        assertThat(totalMessageCount, equalTo(18));
    }

    @Test
    @InRequestScope
    public void getTotalMessageCountTest2() {
        int totalMessageCount =
                versionGroupServiceImpl.getTotalMessageCount(GROUP2_SLUG);
        assertThat(totalMessageCount, equalTo(0));
    }

    @Test
    @InRequestScope
    public void getMaintainersBySlugTest() {
        List<HPerson> maintainers =
                versionGroupServiceImpl.getMaintainersBySlug(GROUP1_SLUG);
        assertThat(maintainers.size(), equalTo(2));
    }

    @Test
    @InRequestScope
    public void isVersionInGroupTest() {
        boolean result =
                versionGroupServiceImpl.isVersionInGroup(GROUP1_SLUG, new Long(
                        1));
        assertThat(result, equalTo(true));

        result =
                versionGroupServiceImpl.isVersionInGroup(GROUP1_SLUG, new Long(
                        3));
        assertThat(result, equalTo(false));
    }

    @Test
    @InRequestScope
    public void getGroupActiveLocalesTest() {
        Set<HLocale> activeLocales =
                versionGroupServiceImpl.getGroupActiveLocales(GROUP1_SLUG);
        assertThat(activeLocales.size(), equalTo(3));

        activeLocales =
                versionGroupServiceImpl.getGroupActiveLocales(GROUP3_SLUG);
        assertThat(activeLocales.size(), equalTo(0));
    }

    @Test
    @InRequestScope
    public void getMissingLocaleVersionMapTest() {
        Map<LocaleId, List<HProjectIteration>> map =
                versionGroupServiceImpl.getMissingLocaleVersionMap(GROUP1_SLUG);

        int activateLocaleSize =
                versionGroupServiceImpl.getGroupActiveLocales(GROUP1_SLUG)
                        .size();
        assertThat(map.size(), equalTo(activateLocaleSize));

        // See ProjectsData.dbunit.xml, HProjectIteration id="900" in group1
        ProjectIterationDAO projectIterationDAO =
                new ProjectIterationDAO(getSession());
        HProjectIteration version = projectIterationDAO.findById(new Long(900));

        for (List<HProjectIteration> versions : map.values()) {
            assertThat("", versions, Matchers.contains(version));
        }
    }
}
