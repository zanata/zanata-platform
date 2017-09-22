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

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.zanata.common.LocaleId;
import org.zanata.exception.ZanataServiceException;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.service.impl.GlossaryFileServiceImpl;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public interface GlossaryFileService extends Serializable {
    /**
     * Save or update all entries {@link org.zanata.rest.dto.GlossaryEntry}
     *
     * @param glossaryEntries
     */
    GlossaryFileServiceImpl.GlossaryProcessed saveOrUpdateGlossary(
            List<GlossaryEntry> glossaryEntries,
            Optional<LocaleId> transLocaleId);

    Map<LocaleId, List<GlossaryEntry>> parseGlossaryFile(
            InputStream inputStream, String fileName,
            LocaleId sourceLang, @Nullable LocaleId transLang,
            String qualifiedName) throws ZanataServiceException;
}
