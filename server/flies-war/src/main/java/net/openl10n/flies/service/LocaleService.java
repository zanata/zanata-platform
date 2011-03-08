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
package net.openl10n.flies.service;

import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.HLocale;

public interface LocaleService
{
   List<HLocale> getAllLocales();

   void save(LocaleId localeId);

   void disable(LocaleId locale);

   void enable(LocaleId locale);

   List<LocaleId> getAllJavaLanguages();

   boolean localeExists(LocaleId locale);
   
   List<HLocale> getSupportedLocales();

   HLocale getByLocaleId(LocaleId locale);

   HLocale validateLocaleByProjectIteration(LocaleId locale, String project, String iterationSlug) throws FliesServiceException;

   List<HLocale> getTranslation(String project, String iterationSlug, String username);
}
