/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service;

import org.zanata.common.LocaleId;
import org.zanata.exception.ZanataServiceException;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles searching and result collation for glossary terms.
 */
public interface GlossarySearchService {

    /**
     * Find glossary entries based on the words in some search text.
     *
     * Searches the global glossary and can include results from a specific
     * project if projectSlug is provided. All entries are sorted with the
     * default sorting.
     *
     * @param srcLocale source locale ID
     * @param transLocale translation locale ID
     * @param searchText search text, interpreted based on searchType
     * @param searchType determines how searchText is interpreted
     * @param maxResults maximum number of results for each of global and
     *                   project search.
     * @param projectSlug (optional) include to run a search against the
     *                    project glossary in addition to the global glossary.
     * @return results that match the search
     */
    ArrayList<GlossaryResultItem> searchGlossary(
            @Nonnull LocaleId srcLocale,
            @Nonnull LocaleId transLocale,
            @Nonnull String searchText,
            @Nonnull SearchType searchType,
            int maxResults,
            @CheckForNull String projectSlug
    ) throws ZanataServiceException;

    /**
     * Get the details for a set of glossary terms.
     *
     * Includes source details, and details from the given locale.
     *
     * @param locale include locale-specific detail for this locale
     * @param sourceIds id for glossary terms in the default locale, found in
     *     results of
     *     {@link #searchGlossary(LocaleId, LocaleId, String, SearchType, int, String)}
     * @return source and target glossary details.
     * @throws ZanataServiceException when locale does not map to a valid locale
     *     in the system.
     */
    ArrayList<GlossaryDetails> lookupDetails(
            @Nonnull LocaleId locale,
            @Nonnull List<Long> sourceIds);
}
