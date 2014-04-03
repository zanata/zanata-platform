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
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@Test(groups = { "business-tests" })
public class LocaleServiceImplTest {
    private LocaleServiceImpl testLocaleServiceImpl;

    @Mock
    private ProjectIterationDAO projectIterationDAO;

    @Mock
    private ProjectDAO projectDAO;

    @Mock
    private LocaleDAO localeDAO;

    @Mock
    private HProject hProject;

    @Mock
    private HProjectIteration hProjectIteration;

    private String projectSlug = "project";
    private String versionSlug = "master";

    @BeforeMethod(firstTimeOnly = true)
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.testLocaleServiceImpl = new LocaleServiceImpl();
        this.testLocaleServiceImpl.setProjectIterationDAO(projectIterationDAO);
        this.testLocaleServiceImpl.setProjectDAO(projectDAO);
        this.testLocaleServiceImpl.setLocaleDAO(localeDAO);

        Set<HLocale> iterationLocales = new HashSet<HLocale>();
        iterationLocales.add(new HLocale(LocaleId.EN_US));
        iterationLocales.add(new HLocale(LocaleId.DE));

        when(hProjectIteration.getId()).thenReturn(1L);
        when(hProjectIteration.getCustomizedLocales()).thenReturn(
                iterationLocales);
        when(projectIterationDAO.getBySlug(projectSlug, versionSlug))
                .thenReturn(hProjectIteration);

        Set<HLocale> projectLocales = new HashSet<HLocale>();
        projectLocales.add(new HLocale(LocaleId.ES));
        when(hProject.getId()).thenReturn(1L);
        when(hProject.getCustomizedLocales()).thenReturn(projectLocales);
        when(projectDAO.getBySlug(projectSlug)).thenReturn(hProject);

        List<HLocale> defaultLocales = new ArrayList<HLocale>();
        defaultLocales.add(new HLocale(LocaleId.FR));
        defaultLocales.add(new HLocale(LocaleId.EN));
        defaultLocales.add(new HLocale(LocaleId.DE));
        when(localeDAO.findAllActiveAndEnabledByDefault()).thenReturn(
                defaultLocales);
    }

    @Test
    public void testGetAllJavaLanguages() throws Exception {
        List<LocaleId> loc = testLocaleServiceImpl.getAllJavaLanguages();
        StringBuilder st = new StringBuilder("");
        for (LocaleId localeId : loc) {
            st.append(localeId.getId() + ",");
        }

        Assert.assertTrue(loc.contains(LocaleId.DE));
        Assert.assertTrue(loc.contains(LocaleId.EN));
        Assert.assertTrue(loc.contains(LocaleId.EN_US));
        Assert.assertTrue(loc.contains(LocaleId.ES));
        Assert.assertTrue(loc.contains(LocaleId.FR));
    }

    @Test
    public void testGetAllSupportedLanguages() {
        List<HLocale> lan = new ArrayList<HLocale>();
        lan.add(new HLocale(new LocaleId("as-IN")));
        lan.add(new HLocale(new LocaleId("pt-BR")));
        when(localeDAO.findAll()).thenReturn(lan);
        List<HLocale> sup = testLocaleServiceImpl.getAllLocales();
        Assert.assertEquals(sup.size(), 2);
        String loc1 = sup.get(0).getLocaleId().getId();
        Assert.assertEquals(loc1, "as-IN");
        String loc2 = sup.get(1).getLocaleId().getId();
        Assert.assertEquals(loc2, "pt-BR");
    }

    @Test
    public void testVersionLocale() {
        when(hProject.isOverrideLocales()).thenReturn(true);
        when(hProjectIteration.isOverrideLocales()).thenReturn(true);

        List<HLocale> result =
                testLocaleServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        assertThat(result, Matchers.hasSize(2));
        assertThat(result.get(0).getLocaleId(),
                Matchers.equalTo(LocaleId.EN_US));
        assertThat(result.get(1).getLocaleId(), Matchers.equalTo(LocaleId.DE));
    }

    @Test
    public void testVersionLocaleWithOverriddenProjectLocales() {
        when(hProject.isOverrideLocales()).thenReturn(true);

        List<HLocale> result =
                testLocaleServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getLocaleId(), Matchers.equalTo(LocaleId.ES));
    }

    @Test
    public void testVersionLocaleWithNoOverridden() throws Exception {
        when(hProjectIteration.isOverrideLocales()).thenReturn(false);
        when(hProject.isOverrideLocales()).thenReturn(false);

        List<HLocale> result =
                testLocaleServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        assertThat(result, Matchers.hasSize(3));

        assertThat(result.get(0).getLocaleId(), Matchers.equalTo(LocaleId.FR));
        assertThat(result.get(1).getLocaleId(), Matchers.equalTo(LocaleId.EN));
        assertThat(result.get(2).getLocaleId(), Matchers.equalTo(LocaleId.DE));
    }

}
