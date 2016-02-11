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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.repository.LocaleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LocaleServiceImplTest {
    private LocaleServiceImpl testLocaleServiceImpl;

    @Mock
    private ProjectIterationDAO projectIterationDAO;

    @Mock
    private ProjectDAO projectDAO;

    @Mock
    private LocaleRepository localeRepo;

    @Mock
    private HProject hProject;

    @Mock
    private HProjectIteration hProjectIteration;

    private String projectSlug = "project";
    private String versionSlug = "master";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.testLocaleServiceImpl = new LocaleServiceImpl();
        this.testLocaleServiceImpl.setProjectIterationDAO(projectIterationDAO);
        this.testLocaleServiceImpl.setProjectDAO(projectDAO);
        this.testLocaleServiceImpl.setLocaleRepository(localeRepo);

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
        when(localeRepo.findAllActiveAndEnabledByDefault()).thenReturn(
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
        when(localeRepo.findAll()).thenReturn(lan);
        List<HLocale> sup = testLocaleServiceImpl.getAllLocales();
        assertThat(sup.size()).isEqualTo(2);
        String loc1 = sup.get(0).getLocaleId().getId();
        assertThat(loc1).isEqualTo("as-IN");
        String loc2 = sup.get(1).getLocaleId().getId();
        assertThat(loc2).isEqualTo("pt-BR");
    }

    @Test
    public void testVersionLocale() {
        when(hProject.isOverrideLocales()).thenReturn(true);
        when(hProjectIteration.isOverrideLocales()).thenReturn(true);

        List<HLocale> result =
                testLocaleServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        assertThat(result).extracting("localeId").containsOnly(
                LocaleId.EN_US, LocaleId.DE);
    }

    @Test
    public void testVersionLocaleWithOverriddenProjectLocales() {
        when(hProject.isOverrideLocales()).thenReturn(true);

        List<HLocale> result =
                testLocaleServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        assertThat(result).extracting("localeId").containsOnly(
                LocaleId.ES);
    }

    @Test
    public void testVersionLocaleWithNoOverridden() throws Exception {
        when(hProjectIteration.isOverrideLocales()).thenReturn(false);
        when(hProject.isOverrideLocales()).thenReturn(false);

        List<HLocale> result =
                testLocaleServiceImpl.getSupportedLanguageByProjectIteration(
                        projectSlug, versionSlug);

        assertThat(result).extracting("localeId").containsOnly(
                LocaleId.FR, LocaleId.EN, LocaleId.DE);
    }

}
