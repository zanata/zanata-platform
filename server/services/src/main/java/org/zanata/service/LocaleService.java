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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.GenericEntity;

import com.google.common.collect.Lists;
import com.ibm.icu.util.ULocale;
import org.zanata.common.LocaleId;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.LanguageTeamSearchResult;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.editor.dto.LocaleSortField;

public interface LocaleService extends Serializable {
    List<HLocale> getAllLocales(int offset, int maxResults, String filter,
            List<LocaleSortField> sortFields);

    List<HLocale> getAllLocales();

    List<HLocale> getSupportedLocales(int offset, int maxResults, String filter,
            List<LocaleSortField> sortFields);

    int getSupportedLocalesTotalCount(String filter);

    int getLocalesTotalCount(String filter);

    void save(@Nonnull LocaleId localeId, boolean enabledByDefault);

    /**
     * Try to delete a locale from Zanata instance.
     *
     * @param localeId
     */
    void delete(@Nonnull LocaleId localeId);

    void disable(@Nonnull LocaleId locale);

    void enable(@Nonnull LocaleId locale);

    List<LocaleId> getAllJavaLanguages();

    boolean localeExists(@Nonnull LocaleId locale);

    boolean localeSupported(@Nonnull LocaleId locale);

    List<HLocale> getSupportedLocales();

    List<HLocale> getSupportedAndEnabledLocales();

    HLocale getByLocaleId(@Nonnull LocaleId locale);

    @Nullable
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

    List<HLocale>
    getSupportedLanguageByProject(@Nonnull HProject project);

    List<HLocale> getTranslation(@Nonnull String project,
            @Nonnull String iterationSlug, String username);

    List<HLocale> getSupportedLanguageByProjectIteration(
            @Nonnull String projectSlug, @Nonnull String iterationSlug);

    List<HLocale> getSupportedLanguageByProjectIteration(
            @Nonnull HProjectIteration version);

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

    static boolean isRTL(LocaleId localeId) {
        ULocale uLocale = new ULocale(localeId.getId());
        return uLocale.isRightToLeft();
    }

    static LocaleDetails convertHLocaleToDTO(HLocale hLocale, String alias) {
        return new LocaleDetails(hLocale.getLocaleId(),
                hLocale.retrieveDisplayName(), alias,
                hLocale.retrieveNativeName(), hLocale.isActive(),
                hLocale.isEnabledByDefault(), hLocale.getPluralForms(),
                isRTL(hLocale.getLocaleId()));
    }

    static LocaleDetails convertHLocaleToDTO(HLocale hLocale) {
        return LocaleService.convertHLocaleToDTO(hLocale, null);
    }

    static Object buildLocaleDetailsListEntity(List<HLocale> locales,
            Map<LocaleId, String> localeAliases) {
        List<LocaleDetails> localeDetails =
                Lists.newArrayListWithExpectedSize(locales.size());

        for (HLocale hLocale : locales) {
            LocaleId id = hLocale.getLocaleId();
            String alias = localeAliases.get(id);
            localeDetails.add(convertHLocaleToDTO(hLocale, alias));
        }
        return new GenericEntity<List<LocaleDetails>>(localeDetails){};
    }

    static LanguageTeamSearchResult convertHLocaleToSearchResultDTO(
            HLocale hLocale) {
        LanguageTeamSearchResult result = new LanguageTeamSearchResult();
        result.setId(hLocale.getLocaleId().getId());
        result.setLocaleDetails(new LocaleDetails(hLocale.getLocaleId(),
                hLocale.retrieveDisplayName(), null,
                hLocale.retrieveNativeName(),
                hLocale.isActive(), hLocale.isEnabledByDefault(),
                hLocale.getPluralForms(), isRTL(hLocale.getLocaleId())));
        Set<HLocaleMember> members = hLocale.getMembers();
        int count = members == null ? 0 : members.size();
        result.setMemberCount(count);
        result.setRequestCount(0L);
        return result;
    }
}
