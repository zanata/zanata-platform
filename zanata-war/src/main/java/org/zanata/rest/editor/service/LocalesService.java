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

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.util.GenericType;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.model.HLocale;
import org.zanata.rest.editor.dto.Locale;
import org.zanata.rest.editor.service.resource.LocalesResource;
import org.zanata.service.LocaleService;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.localesService")
@Path(LocalesResource.SERVICE_PATH)
@Transactional
public class LocalesService implements LocalesResource {

    @Inject
    private LocaleService localeServiceImpl;

    @Override
    public Response get() {
        List<HLocale> locales = localeServiceImpl.getAllLocales();

        List<Locale> localesRefs =
                Lists.newArrayListWithExpectedSize(locales.size());

        for (HLocale hLocale : locales) {
            localesRefs.add(new Locale(hLocale.getLocaleId(),
                    hLocale.retrieveDisplayName()));
        }

        Type genericType = new GenericType<List<Locale>>() {
        }.getGenericType();
        Object entity =
                new GenericEntity<List<Locale>>(localesRefs, genericType);
        return Response.ok(entity).build();
    }

}
