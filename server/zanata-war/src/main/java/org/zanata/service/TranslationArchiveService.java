/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import org.zanata.async.AsyncTaskHandle;

/**
 * This service deals with the archiving / unarchiving of bundles that may
 * contain multiple translation files.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface TranslationArchiveService {

    /**
     * Builds an archive file with translation files for a given project
     * iteration in a given language. The contents may be altered depending on
     * the project type.
     *
     * @param projectSlug
     * @param iterationSlug
     * @param localeId
     * @param userName
     * @param handle
     * @return A file identifier
     */
    String buildTranslationFileArchive(@Nonnull String projectSlug,
            @Nonnull String iterationSlug,
            @Nonnull String localeId, @Nonnull String userName,
            AsyncTaskHandle<String> handle)
            throws Exception;

    /**
     * Asynchronously starts building a project archive. Should have the same
     * result as
     * {@link org.zanata.service.TranslationArchiveService#buildTranslationFileArchive(String, String, String, String, org.zanata.async.AsyncTaskHandle)}
     * but performed in the background.
     *
     * @param projectSlug
     * @param iterationSlug
     * @param localeId
     * @param userName
     * @param handle
     * @return
     * @see org.zanata.service.TranslationArchiveService#buildTranslationFileArchive(String,
     *      String, String, String, org.zanata.async.AsyncTaskHandle)
     */
    Future<String> startBuildingTranslationFileArchive(
            @Nonnull String projectSlug,
            @Nonnull String iterationSlug, @Nonnull String localeId,
            @Nonnull String userName,
            AsyncTaskHandle<String> handle) throws Exception;
}
