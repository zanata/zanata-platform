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
package org.zanata.rest.service;

import com.google.common.collect.Lists;
import org.jboss.resteasy.util.GenericType;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.service.LocaleService;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Parent class for endpoints that return a list of locales.
 */
public abstract class LocalesService {

    protected Object buildLocaleDetailsListEntity(List<HLocale> locales, Map<LocaleId, String> localeAliases) {
        List<LocaleDetails> localeDetails =
                Lists.newArrayListWithExpectedSize(locales.size());

        for (HLocale hLocale : locales) {
            LocaleId id = hLocale.getLocaleId();
            String name = hLocale.retrieveDisplayName();
            String alias = localeAliases.get(id);
            localeDetails.add(new LocaleDetails(id, name, alias));
        }

        Type genericType = new GenericType<List<LocaleDetails>>() {
        }.getGenericType();
        return new GenericEntity<List<LocaleDetails>>(localeDetails, genericType);
    }

}
