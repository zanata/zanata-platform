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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.editor.dto.LocaleSortField;
import org.zanata.service.LocaleService;
import org.zanata.servlet.annotations.AllJavaLocales;
import org.zanata.util.ComparatorUtil;
import com.google.common.collect.Maps;
import com.ibm.icu.util.ULocale;

/**
 * This implementation provides all the business logic related to Locale.
 */
@Named("localeServiceImpl")
@RequestScoped
@Transactional
public class LocaleServiceImpl implements LocaleService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LocaleServiceImpl.class);
    private LocaleDAO localeDAO;
    private ProjectDAO projectDAO;
    private ProjectIterationDAO projectIterationDAO;
    private PersonDAO personDAO;
    private VersionGroupDAO versionGroupDAO;
    private TextFlowTargetDAO textFlowTargetDAO;

    public LocaleServiceImpl() {
    }

    public LocaleServiceImpl(LocaleDAO localeDAO, ProjectDAO projectDAO,
            ProjectIterationDAO projectIterationDAO, PersonDAO personDAO,
            TextFlowTargetDAO textFlowTargetDAO,
            VersionGroupDAO versionGroupDAO) {
        setLocaleDAO(localeDAO);
        setProjectDAO(projectDAO);
        setProjectIterationDAO(projectIterationDAO);
        setPersonDAO(personDAO);
        setTextFlowTargetDAO(textFlowTargetDAO);
        setVersionGroupDAO(versionGroupDAO);
    }

    public static Map<LocaleId, String>
            getLocaleAliasesByIteration(HProjectIteration iteration) {
        Map<LocaleId, String> localeAliases;
        if (iteration.isOverrideLocales()) {
            localeAliases = iteration.getLocaleAliases();
        } else {
            localeAliases = iteration.getProject().getLocaleAliases();
        }
        return localeAliases;
    }

    @Inject
    public void setTextFlowTargetDAO(TextFlowTargetDAO textFlowTargetDAO) {
        this.textFlowTargetDAO = textFlowTargetDAO;
    }

    @Inject
    public void setLocaleDAO(LocaleDAO localeDAO) {
        this.localeDAO = localeDAO;
    }

    @Inject
    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    @Inject
    public void
            setProjectIterationDAO(ProjectIterationDAO projectIterationDAO) {
        this.projectIterationDAO = projectIterationDAO;
    }

    @Inject
    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    @Inject
    public void setVersionGroupDAO(VersionGroupDAO versionGroupDAO) {
        this.versionGroupDAO = versionGroupDAO;
    }

    public List<HLocale> getAllLocales() {
        return localeDAO.find(0, -1, null, null, false);
    }

    @Override
    public List<HLocale> getAllLocales(int offset, int maxResults,
            String filter, List<LocaleSortField> sortFields) {
        return localeDAO.find(offset, maxResults, filter, sortFields, false);
    }

    @Override
    public List<HLocale> getSupportedLocales(int offset, int maxResults,
            String filter, List<LocaleSortField> sortFields) {
        return localeDAO.find(offset, maxResults, filter, sortFields, true);
    }

    @Override
    public int getSupportedLocalesTotalCount(String filter) {
        return localeDAO.countByFind(filter, true);
    }

    @Override
    public int getLocalesTotalCount(String filter) {
        return localeDAO.countByFind(filter, false);
    }

    @Override
    public void save(@Nonnull LocaleId localeId, boolean enabledByDefault) {
        if (localeExists(localeId))
            return;
        HLocale entity = new HLocale();
        entity.setLocaleId(localeId);
        entity.setActive(true);
        entity.setEnabledByDefault(enabledByDefault);
        localeDAO.makePersistent(entity);
        localeDAO.flush();
    }

    @Override
    public void delete(@Nonnull LocaleId localeId) {
        HLocale entity = getByLocaleId(localeId);
        if (entity == null) {
            return;
        }
        localeDAO.makeTransient(entity);
        localeDAO.flush();
    }

    @Override
    public void disable(@Nonnull LocaleId localeId) {
        HLocale entity = localeDAO.findByLocaleId(localeId);
        if (entity != null) {
            entity.setActive(false);
            localeDAO.makePersistent(entity);
            localeDAO.flush();
        }
    }

    @Produces
    @ApplicationScoped
    @AllJavaLocales
    public List<LocaleId> getAllJavaLanguages() {
        List<LocaleId> allJavaLanguages = new ArrayList<LocaleId>();
        ULocale[] locales = ULocale.getAvailableLocales();
        allJavaLanguages = new ArrayList<LocaleId>();
        for (ULocale locale : locales) {
            String id = locale.toLanguageTag();
            LocaleId localeId = new LocaleId(id);
            allJavaLanguages.add(localeId);
        }
        return allJavaLanguages;
    }

    public void enable(@Nonnull LocaleId localeId) {
        HLocale entity = localeDAO.findByLocaleId(localeId);
        if (entity != null) {
            entity.setActive(true);
            localeDAO.makePersistent(entity);
            localeDAO.flush();
        }
    }

    public boolean localeExists(@Nonnull LocaleId locale) {
        HLocale entity = localeDAO.findByLocaleId(locale);
        return entity != null;
    }

    @Override
    public List<HLocale> getSupportedLocales() {
        List<HLocale> activeLocales = localeDAO.findAllActive();
        Collections.sort(activeLocales, ComparatorUtil.LOCALE_COMPARATOR);
        return activeLocales;
    }

    @Override
    public List<HLocale> getSupportedAndEnabledLocales() {
        List<HLocale> locales = localeDAO.findAllActiveAndEnabledByDefault();
        Collections.sort(locales, ComparatorUtil.LOCALE_COMPARATOR);
        return locales;
    }

    @Override
    public boolean localeSupported(@Nonnull LocaleId locale) {
        HLocale entity = localeDAO.findByLocaleId(locale);
        return entity != null && entity.isActive();
    }

    @Override
    @Nonnull
    public HLocale validateLocaleByProject(@Nonnull LocaleId locale,
            @Nonnull String project) throws ZanataServiceException {
        List<HLocale> allList = getSupportedLanguageByProject(project);
        HLocale hLocale = localeDAO.findByLocaleId(locale);
        if (hLocale == null || !hLocale.isActive()) {
            throw new ZanataServiceException(
                    "Locale " + locale.getId()
                            + " is not enabled on this server. Please contact admin.",
                    403);
        }
        if (!allList.contains(hLocale)) {
            throw new ZanataServiceException(
                    "Locale " + locale.getId() + " is not allowed for project "
                            + project + ". Please contact project maintainer.",
                    403);
        }
        return hLocale;
    }

    @Override
    @Nonnull
    public HLocale validateLocaleByProjectIteration(@Nonnull LocaleId locale,
            @Nonnull String project, @Nonnull String iterationSlug)
            throws ZanataServiceException {
        List<HLocale> allList =
                getSupportedLanguageByProjectIteration(project, iterationSlug);
        HLocale hLocale = localeDAO.findByLocaleId(locale);
        if (hLocale == null || !hLocale.isActive()) {
            throw new ZanataServiceException(
                    "Locale " + locale.getId()
                            + " is not enabled on this server. Please contact admin.",
                    403);
        }
        if (!allList.contains(hLocale)) {
            throw new ZanataServiceException("Locale " + locale.getId()
                    + " is not allowed for project " + project + " and version "
                    + iterationSlug + ". Please contact project maintainer.",
                    403);
        }
        return hLocale;
    }

    @Override
    public HLocale validateSourceLocale(LocaleId locale)
            throws ZanataServiceException {
        HLocale hLocale = getByLocaleId(locale);
        if (hLocale == null || !hLocale.isActive()) {
            throw new ZanataServiceException(
                    "Locale " + locale.getId()
                            + " is not enabled on this server. Please contact admin.",
                    403);
        }
        return hLocale;
    }

    @Override
    public HLocale getByLocaleId(@Nonnull LocaleId locale) {
        return localeDAO.findByLocaleId(locale);
    }

    @Override
    @Nullable
    public HLocale getByLocaleId(@Nonnull String localeId) {
        try {
            return this.getByLocaleId(new LocaleId(localeId));
        } catch (IllegalArgumentException e) {
            log.warn("Tried to look up a locale with a malformed id", e);
            return null;
        }
    }

    @Override
    public List<HLocale> getSupportedLanguageByProjectIteration(
            @Nonnull String projectSlug, @Nonnull String iterationSlug) {
        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        if (iteration != null && iteration.isOverrideLocales()) {
            return new ArrayList<HLocale>(iteration.getCustomizedLocales());
        }
        return getSupportedLanguageByProject(projectSlug);
    }

    @Override
    public List<HLocale>
            getSupportedLanguageByProject(@Nonnull String projectSlug) {
        HProject proj = projectDAO.getBySlug(projectSlug);
        if (proj != null && proj.isOverrideLocales()) {
            return new ArrayList<HLocale>(proj.getCustomizedLocales());
        }
        return localeDAO.findAllActiveAndEnabledByDefault();
    }

    @Override
    public List<HLocale> getTranslation(@Nonnull String project,
            @Nonnull String iterationSlug, String username) {
        List<HLocale> allList =
                getSupportedLanguageByProjectIteration(project, iterationSlug);
        List<HLocale> member =
                personDAO.getLanguageMembershipByUsername(username);
        member.retainAll(allList);
        return member;
    }

    private String getDescript(HLocale op) {
        return op.retrieveDisplayName() + " [" + op.getLocaleId().getId() + "] "
                + op.retrieveNativeName();
    }

    @Override
    public Map<String, String> getGlobalLocaleItems() {
        Map<String, String> globalItems = new TreeMap<String, String>();
        List<HLocale> locale = getSupportedLocales();
        for (HLocale op : locale) {
            String name = getDescript(op);
            globalItems.put(name, name);
        }
        return globalItems;
    }

    @Override
    public Map<String, String>
            getIterationGlobalLocaleItems(String projectSlug) {
        HProject project = projectDAO.getBySlug(projectSlug);
        return project.isOverrideLocales()
                ? getCustomizedLocalesItems(projectSlug)
                : getGlobalLocaleItems();
    }

    @Override
    public Map<String, String> getCustomizedLocalesItems(String projectSlug) {
        Map<String, String> customizedItems = new TreeMap<String, String>();
        HProject project = projectDAO.getBySlug(projectSlug);
        if (project != null && project.isOverrideLocales()) {
            Set<HLocale> locales = project.getCustomizedLocales();
            for (HLocale op : locales) {
                String name = getDescript(op);
                customizedItems.put(name, name);
            }
        }
        return customizedItems;
    }

    @Override
    public Map<String, String> getDefaultCustomizedLocalesItems() {
        Map<String, String> defaultItems = new TreeMap<String, String>();
        List<HLocale> allLocales = getSupportedLocales();
        for (HLocale locale : allLocales) {
            if (locale.isEnabledByDefault()) {
                String desc = getDescript(locale);
                defaultItems.put(desc, desc);
            }
        }
        return defaultItems;
    }

    @Override
    public Map<String, String> getIterationCustomizedLocalesItems(
            String projectSlug, String iterationSlug) {
        Map<String, String> customizedItems = Maps.newTreeMap();
        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        if (iteration != null && iteration.isOverrideLocales()) {
            Set<HLocale> locales = iteration.getCustomizedLocales();
            for (HLocale op : locales) {
                String name = getDescript(op);
                customizedItems.put(name, name);
            }
        }
        return customizedItems;
    }

    @Override
    public Set<HLocale> convertCustomizedLocale(Map<String, String> var) {
        Set<HLocale> result = new HashSet<HLocale>();
        for (String op : var.keySet()) {
            String[] list = op.split("\\[");
            String seVar = list[1].split("\\]")[0];
            HLocale entity = localeDAO.findByLocaleId(new LocaleId(seVar));
            result.add(entity);
        }
        return result;
    }

    public HLocale getSourceLocale(String projectSlug, String iterationSlug) {
        return localeDAO.findByLocaleId(new LocaleId("en-US"));
    }

    @Override
    public HTextFlowTarget getLastTranslated(String projectSlug,
            String iterationSlug, LocaleId localeId) {
        return textFlowTargetDAO.getLastTranslated(projectSlug, iterationSlug,
                localeId);
    }

    public static LocaleDetails convertToDTO(HLocale hLocale, String alias) {
        return new LocaleDetails(hLocale.getLocaleId(),
                hLocale.retrieveDisplayName(), alias,
                hLocale.retrieveNativeName(), hLocale.isActive(),
                hLocale.isEnabledByDefault(), hLocale.getPluralForms());
    }

    public static LocaleDetails convertToDTO(HLocale hLocale) {
        return convertToDTO(hLocale, null);
    }

    public static HLocale convertToHLocale(LocaleDetails localeDetails) {
        HLocale entity = new HLocale();
        entity.setLocaleId(localeDetails.getLocaleId());
        entity.setActive(localeDetails.isEnabled());
        entity.setEnabledByDefault(localeDetails.isEnabledByDefault());
        entity.setPluralForms(localeDetails.getPluralForms());
        entity.setDisplayName(localeDetails.getDisplayName());
        entity.setNativeName(localeDetails.getNativeName());
        return entity;
    }
}
