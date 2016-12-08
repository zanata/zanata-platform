/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.editor.service;

import com.google.common.base.Joiner;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.rest.editor.dto.suggestion.Suggestion;
import org.zanata.rest.editor.service.resource.SuggestionsResource;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.webtrans.shared.model.TransMemoryQuery;

import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.zanata.webtrans.shared.rpc.HasSearchType.*;

/**
 * @see org.zanata.rest.editor.service.resource.SuggestionsResource
 */
@RequestScoped
@Named("editor.suggestionsService")
@Path(SuggestionsResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class SuggestionsService implements SuggestionsResource {

    public static final String SEARCH_TYPES = Joiner.on(", ").join(SearchType.values());

    @Inject
    private TranslationMemoryService transMemoryService;

    @Inject
    private LocaleService localeService;

    @Override
    public Response query(List<String> query, String sourceLocaleString,
            String transLocaleString, String searchTypeString, long textFlowTargetId) {

        Optional<SearchType> searchType = getSearchType(searchTypeString);
        if (!searchType.isPresent()) {
            return unknownSearchTypeResponse(searchTypeString);
        }

        Optional<LocaleId> sourceLocale = getLocale(sourceLocaleString);
        if (!sourceLocale.isPresent()) {
            return Response.status(BAD_REQUEST)
                    .entity(String.format("Unrecognized source locale: \"%s\"", sourceLocaleString))
                    .build();
        }

        Optional<LocaleId> transLocale = getLocale(transLocaleString);
        if (!transLocale.isPresent()) {
            return Response.status(BAD_REQUEST)
                    .entity(String.format("Unrecognized translation locale: \"%s\"", transLocaleString))
                    .build();
        }

        Optional<Long> tft;
        if (textFlowTargetId < 0) {
            tft = Optional.empty();
        } else {
            tft = Optional.of(textFlowTargetId);
        }
        List<Suggestion> suggestions = transMemoryService.searchTransMemoryWithDetails(transLocale.get(),
                sourceLocale.get(), new TransMemoryQuery(query, searchType.get()), tft);

        // Wrap in generic entity to prevent type erasure, so that an
        // appropriate MessageBodyReader can be used.
        // see docs for GenericEntity
        GenericEntity<List<Suggestion>> entity = new GenericEntity<List<Suggestion>>(suggestions) {};

        return Response.ok(entity).build();
    }

    /**
     * Try to get a valid locale for a given string.
     *
     * @param localeString used to look up the locale
     * @return a wrapped LocaleId if the given string matches one, otherwise an empty option.
     */
    private Optional<LocaleId> getLocale(String localeString) {
        @Nullable HLocale hLocale = localeService.getByLocaleId(localeString);
        if (hLocale == null) {
            return Optional.empty();
        }
        return Optional.of(hLocale.getLocaleId());
    }

    /**
     * Try to get a valid search type constant for a given string.
     *
     * @param searchTypeString used to look up the search type. Case insensitive.
     * @return A wrapped SearchType if the given string matches one, otherwise an empty option.
     */
    private Optional<SearchType> getSearchType(String searchTypeString) {
        for (SearchType type : SearchType.values()) {
            if (type.name().equalsIgnoreCase(searchTypeString)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Generate and build an error response that reports the search type being unrecognized.
     *
     * @param searchTypeString shown in the error message as the unrecognized string
     * @return a built Response.
     */
    private Response unknownSearchTypeResponse(String searchTypeString) {
        String error = String.format("Unrecognized search type: \"%s\". Expected one of: %s",
                searchTypeString, SEARCH_TYPES);
        return Response.status(BAD_REQUEST).entity(error).build();
    }
}
