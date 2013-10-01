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
package org.zanata.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.zanata.common.LocaleId;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlowTarget;

public interface LocaleService {
    List<HLocale> getAllLocales();

    void save(@Nonnull LocaleId localeId, boolean enabledByDefault);

    void disable(@Nonnull LocaleId locale);

    void enable(@Nonnull LocaleId locale);

    List<LocaleId> getAllJavaLanguages();

    boolean localeExists(@Nonnull LocaleId locale);

    boolean localeSupported(@Nonnull LocaleId locale);

    List<HLocale> getSupportedLocales();

    HLocale getByLocaleId(@Nonnull LocaleId locale);

    HLocale getByLocaleId(@Nonnull String localeId);

    @Nonnull
    HLocale validateLocaleByProjectIteration(@Nonnull LocaleId locale,
            @Nonnull String project, @Nonnull String iterationSlug)
            throws ZanataServiceException;

    @Nonnull
    HLocale validateLocaleByProject(@Nonnull LocaleId locale,
            @Nonnull String project) throws ZanataServiceException;

    // TODO I don't think this method is specifically about source languages
    HLocale validateSourceLocale(LocaleId locale) throws ZanataServiceException;

    List<HLocale> getTranslation(@Nonnull String project,
            @Nonnull String iterationSlug, String username);

    List<HLocale> getSupportedLangugeByProjectIteration(
            @Nonnull String project, @Nonnull String iterationSlug);

    List<HLocale> getSupportedLanguageByProject(@Nonnull String project);

    Map<String, String> getGlobalLocaleItems();

    Map<String, String> getCustomizedLocalesItems(String project);

    /**
     * @return The default selected locale items.
     */
    Map<String, String> getDefaultCustomizedLocalesItems();

    Set<HLocale> convertCustomizedLocale(Map<String, String> var);

    Map<String, String> getIterationCustomizedLocalesItems(String projectSlug,
            String iterationSlug);

    Map<String, String> getIterationGlobalLocaleItems(String projectSlug);

    HLocale getSourceLocale(String projectSlug, String iterationSlug);

    HTextFlowTarget getLastTranslated(String projectSlug, String iterationSlug,
            LocaleId localeId);

}
