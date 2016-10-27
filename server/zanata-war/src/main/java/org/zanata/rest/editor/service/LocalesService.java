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
package org.zanata.rest.editor.service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.resteasy.util.GenericType;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.GlossarySortField;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.editor.dto.LocaleSortField;
import org.zanata.rest.editor.dto.LocalesResults;
import org.zanata.rest.editor.service.resource.LocalesResource;
import org.zanata.rest.search.dto.LanguageTeamSearchResult;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;

import com.google.common.collect.Lists;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.webtrans.shared.model.Locale;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.localesService")
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

        if (identity != null && identity.hasRole("admin")) {
            locales = localeServiceImpl.getAllLocales(offset, validatedPageSize,
                    filter, convertToSortField(fields));
            totalCount = localeServiceImpl.getLocalesTotalCount(filter);
        } else {
            locales = localeServiceImpl.getSupportedLocales(offset,
                    validatedPageSize, filter, convertToSortField(fields));
            totalCount = localeServiceImpl.getSupportedLocalesTotalCount(filter);
        }
        List<LanguageTeamSearchResult> localesRefs =
            Lists.newArrayListWithExpectedSize(locales.size());

        localesRefs.addAll(locales.stream().map(LanguageTeamSearchResult::new)
                .collect(Collectors.toList()));

        LocalesResults localesResults =
                new LocalesResults(totalCount, localesRefs);
        return Response.ok(localesResults).build();
    }

    @Override
    public Response getDetails(String localeId) {
        if (StringUtils.isBlank(localeId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Locale '" + localeId + "' is required.").build();
        }
        HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
        if (hLocale != null) {
            LocaleDetails details = LocaleServiceImpl.convertToDTO(hLocale, "");
            return Response.ok(details).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Transactional(readOnly = true)
    @Override
    public Response getUITranslations() {
        List<HLocale> locales = localeServiceImpl.getSupportedLocales();
        List<LocaleDetails> localesRefs =
            Lists.newArrayListWithExpectedSize(locales.size());
        localesRefs.addAll(
            locales.stream()
                .map(hLocale -> LocaleServiceImpl.convertToDTO(hLocale, ""))
                .collect(Collectors.toList()));
        Type genericType = new GenericType<List<LocaleDetails>>() {
        }.getGenericType();
        Object entity =
            new GenericEntity<List<LocaleDetails>>(localesRefs, genericType);
        return Response.ok(entity).build();
    }

    @Transactional
    @Override
    public Response delete(String localeId) {
        if (StringUtils.isBlank(localeId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Locale '" + localeId + "' is required.").build();
        }
        LocaleId locale = new LocaleId(localeId);
        identity.checkPermission("delete-language");

        try {
//            localeServiceImpl.delete(locale);
            return Response.ok().build();
        } catch (ConstraintViolationException e) {
            return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        }
    }

    private int validatePage(int page) {
        return page < 1 ? 1 : page;
    }

    private int validatePageSize(int sizePerPage) {
        return (sizePerPage > MAX_PAGE_SIZE) ? MAX_PAGE_SIZE
            : ((sizePerPage < 1) ? 1 : sizePerPage);
    }

    private List<LocaleSortField> convertToSortField(
        String commaSeparatedFields) {
        List<LocaleSortField> result = Lists.newArrayList();

        String[] fields = StringUtils.split(commaSeparatedFields, ",");
        if(fields == null || fields.length <= 0) {
            //default sorting
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
}
