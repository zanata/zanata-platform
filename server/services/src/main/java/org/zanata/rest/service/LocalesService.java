/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.rest.dto.LanguageTeamSearchResult;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.LocaleMember;
import org.zanata.rest.dto.SourceLocaleDetails;
import org.zanata.rest.editor.dto.LocaleSortField;
import org.zanata.rest.dto.LocalesResults;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckRole;
import org.zanata.service.LocaleService;
import com.google.common.collect.Lists;
import org.zanata.service.RequestService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.servlet.annotations.AllJavaLocales;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("localesService")
@Path(LocalesResource.SERVICE_PATH)
public class LocalesService implements LocalesResource {

    /**
     * Maximum result for per page.
     */
    public static final int MAX_PAGE_SIZE = 100;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    @AllJavaLocales
    private List<LocaleId> allJavaLocales;
    @Inject
    RequestService requestService;

    @Transactional(readOnly = true)
    @Override
    public Response get(@QueryParam("filter") String filter,
            @QueryParam("sort") String fields,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("sizePerPage") int sizePerPage) {
        List<HLocale> locales;
        int totalCount;
        int validatedPageSize = validatePageSize(sizePerPage);
        int offset = (validatePage(page) - 1) * validatedPageSize;
        boolean isAdmin = identity != null && identity.hasRole("admin");
        if (isAdmin) {
            locales = localeServiceImpl.getAllLocales(offset, validatedPageSize,
                    filter, convertToSortField(fields));
            totalCount = localeServiceImpl.getLocalesTotalCount(filter);
        } else {
            locales = localeServiceImpl.getSupportedLocales(offset,
                    validatedPageSize, filter, convertToSortField(fields));
            totalCount =
                    localeServiceImpl.getSupportedLocalesTotalCount(filter);
        }
        List<LanguageTeamSearchResult> localesRefs = locales.stream()
                .map(hLocale -> LocaleService.convertHLocaleToSearchResultDTO(hLocale))
                .collect(Collectors.toList());
        LocalesResults localesResults =
                new LocalesResults(totalCount, localesRefs);

        if (isAdmin) {
            List<LocaleId> localeIds = localeDAO.findAll().stream()
                    .map(hLocale -> hLocale.getLocaleId()).collect(Collectors.toList());
            // Map all requests to localeIds, to prevent multiple requests
            Map<LocaleId, Long> allRequests =
                    requestService.getPendingLanguageRequests(
                            localeIds.toArray(new LocaleId[localeIds.size()]))
                            .stream().collect(Collectors.groupingBy(languageRequest ->
                            languageRequest.getLocale().getLocaleId(), Collectors.counting()));
            for (LanguageTeamSearchResult searchResult : localesRefs) {
                searchResult.setRequestCount(firstNonNull(allRequests.get(
                        searchResult.getLocaleDetails().getLocaleId()), 0L));
            }
        }
        return Response.ok(localesResults).build();
    }

    private final FilterLocaleDetails filterLocaleDetails =
            new FilterLocaleDetails();

    @Transactional(readOnly = true)
    @Override
    public Response getNewLocales(@QueryParam("filter") String filter,
            @QueryParam("size") @DefaultValue("10") int size) {
        List<LocaleId> supportedLocaleIds = localeServiceImpl.getAllLocales()
                .stream().map(hLocale -> hLocale.getLocaleId())
                .collect(Collectors.toList());
        List<LocaleDetails> localeDetails = allJavaLocales.stream()
                .filter(localeId -> !supportedLocaleIds.contains(localeId))
                .map(convertToLocaleDetails).collect(Collectors.toList());
        if (StringUtils.isNotBlank(filter)) {
            filterLocaleDetails.setQuery(filter);
            localeDetails = localeDetails.stream().filter(filterLocaleDetails)
                    .collect(Collectors.toList());
        }
        size = size <= 0 ? 10 : size;
        if (localeDetails.size() > size) {
            localeDetails = localeDetails.subList(0, size);
        }
        Object entity = new GenericEntity<List<LocaleDetails>>(localeDetails){};
        return Response.ok(entity).build();
    }

    @Transactional(readOnly = true)
    @Override
    public Response getDetails(String localeId) {
        if (StringUtils.isBlank(localeId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Locale \'" + localeId + "\' is required.").build();
        }
        HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
        if (hLocale != null) {
            LocaleDetails details = LocaleService.convertHLocaleToDTO(hLocale);
            return Response.ok(details).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Transactional(readOnly = true)
    @Override
    public Response getMembers(String localeId) {
        if (StringUtils.isBlank(localeId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Locale \'" + localeId + "\' is required.").build();
        }
        HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
        if (hLocale != null) {
            Set<HLocaleMember> members = hLocale.getMembers();
            List<LocaleMember> results = members != null ?
                    members.stream().map(convertToLocaleMember)
                            .collect(Collectors.toList()) :
                    Lists.newArrayList();
            return Response.ok(results).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Transactional(readOnly = true)
    @Override
    public Response getUITranslations() {
        List<HLocale> locales = localeServiceImpl.getSupportedLocales();
        List<LocaleDetails> localesRefs = locales.stream()
                .map(hLocale -> LocaleService.convertHLocaleToDTO(hLocale))
                .collect(Collectors.toList());
        Object entity = new GenericEntity<List<LocaleDetails>>(localesRefs){};
        return Response.ok(entity).build();
    }

    @Transactional(readOnly = true)
    @Override
    @CheckRole("admin")
    public Response getSourceLocales() {
        identity.checkPermission("read-source-language");
        Map<HLocale, Integer> locales = localeDAO.getAllSourceLocalesAndDocCount();

        List<SourceLocaleDetails> results = new ArrayList<>();

        for (Map.Entry<HLocale, Integer> entry: locales.entrySet()) {
            LocaleDetails details = LocaleService.convertHLocaleToDTO(entry.getKey());
            results.add(new SourceLocaleDetails(entry.getValue(), details));
        }
        if (results.size() > 1) {
            // Adding total doc count to the result set
            int count = documentDAO.getTotalDocCount();
            results.add(new SourceLocaleDetails(count, null));
        }
        return Response
                .ok(new GenericEntity<List<SourceLocaleDetails>>(results) {
                }).build();
    }

    @Transactional
    @Override
    public Response delete(String localeId) {
        if (StringUtils.isBlank(localeId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Locale \'" + localeId + "\' is required.").build();
        }
        LocaleId locale = new LocaleId(localeId);
        identity.checkPermission("delete-language");
        try {
            localeServiceImpl.delete(locale);
            return Response.ok().build();
        } catch (ConstraintViolationException e) {
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        }
    }

    @Transactional
    @Override
    public Response createLanguage(LocaleDetails localeDetails) {
        identity.checkPermission("insert-language");
        if (localeServiceImpl.localeExists(localeDetails.getLocaleId())) {
            return Response.ok().build();
        }
        Matcher matcher = Pattern.compile("[\\w\\d@.-]+")
                .matcher(localeDetails.getLocaleId().toJavaName());
        if (!matcher.matches()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid Locale ID").build();
        }
        HLocale hLocale = LocaleServiceImpl.convertToHLocale(localeDetails);
        localeDAO.makePersistent(hLocale);
        localeDAO.flush();
        return Response.status(Response.Status.CREATED)
                .entity(LocaleServiceImpl.convertToDTO(hLocale)).build();
    }

    private int validatePage(int page) {
        return page < 1 ? 1 : page;
    }

    private int validatePageSize(int sizePerPage) {
        return (sizePerPage > MAX_PAGE_SIZE) ? MAX_PAGE_SIZE
                : ((sizePerPage < 1) ? 1 : sizePerPage);
    }

    private List<LocaleSortField>
            convertToSortField(String commaSeparatedFields) {
        List<LocaleSortField> result = Lists.newArrayList();
        String[] fields = StringUtils.split(commaSeparatedFields, ",");
        if (fields == null || fields.length <= 0) {
            // default sorting
            result.add(LocaleSortField.getByField(LocaleSortField.LOCALE));
        } else {
            for (String field : fields) {
                LocaleSortField sortField = LocaleSortField.getByField(field);
                if (sortField != null) {
                    result.add(sortField);
                }
            }
        }
        return result;
    }

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private final Function<LocaleId, LocaleDetails> convertToLocaleDetails =
            new Function<LocaleId, LocaleDetails>() {
                @Override
                public LocaleDetails apply(LocaleId localeId) {
                    HLocale hLocale = new HLocale(localeId);
                    hLocale.setDisplayName(hLocale.retrieveDisplayName());
                    hLocale.setNativeName(hLocale.retrieveNativeName());
                    String pluralForms =
                            resourceUtils.getPluralForms(localeId, false, true);
                    hLocale.setPluralForms(pluralForms);
                    return LocaleService.convertHLocaleToDTO(hLocale);
                }
            };

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private final Function<HLocaleMember, LocaleMember> convertToLocaleMember =
            new Function<HLocaleMember, LocaleMember>() {
                @Override
                public LocaleMember apply(HLocaleMember localeMember) {
                    return new LocaleMember(
                            localeMember.getPerson().getAccount().getUsername(),
                            localeMember.isCoordinator(),
                            localeMember.isReviewer(),
                            localeMember.isTranslator());
                }
            };

    private static class FilterLocaleDetails implements Predicate<LocaleDetails>,
            Serializable {

        private static final long serialVersionUID = -1156776442946778719L;
        private String query;

        @Override
        public boolean test(LocaleDetails localeDetails) {
            if (StringUtils.isBlank(query)) {
                return true;
            }
            return StringUtils
                    .containsIgnoreCase(localeDetails.getDisplayName(), query)
                    || StringUtils.containsIgnoreCase(
                            localeDetails.getLocaleId().getId(), query);
        }

        public void setQuery(final String query) {
            this.query = query;
        }
    }
}
