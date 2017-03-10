/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.search.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.LocaleDetails;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LanguageTeamSearchResult extends SearchResult {
    private LocaleDetails localeDetails;
    private long memberCount;

    public LanguageTeamSearchResult() {
        this.setType(SearchResultType.LanguageTeam);
    }

    public LanguageTeamSearchResult(HLocale locale) {
        this.setType(SearchResultType.LanguageTeam);
        this.setId(locale.getLocaleId().getId());
        this.localeDetails = new LocaleDetails(locale.getLocaleId(),
                locale.retrieveDisplayName(), null, locale.retrieveNativeName(),
                locale.isActive(), locale.isEnabledByDefault(),
                locale.getPluralForms());
        this.memberCount = locale.getMembers().size();
    }

    public LocaleDetails getLocaleDetails() {
        return this.localeDetails;
    }

    public void setLocaleDetails(final LocaleDetails localeDetails) {
        this.localeDetails = localeDetails;
    }

    public long getMemberCount() {
        return this.memberCount;
    }

    public void setMemberCount(final long memberCount) {
        this.memberCount = memberCount;
    }
}
