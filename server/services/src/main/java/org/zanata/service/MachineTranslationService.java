/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import org.zanata.async.handle.MachineTranslationPrefillTaskHandle;
import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlow;
import org.zanata.rest.dto.MachineTranslationPrefill;

public interface MachineTranslationService {

    /**
     * Synchronously fetches a machine translation from the default back-end, for the specified TextFlow.
     *
     * @param textFlow
     *         textflow
     * @param fromLocale
     *         source locale
     * @param toLocale
     *         target locale
     * @return machine translation
     */
    List<String> getSuggestion(HTextFlow textFlow, LocaleId fromLocale,
            LocaleId toLocale);

    /**
     * Asynchronously fetches machine translations from the default back-end, for all New/Untranslated TextFlows in the Project Version. Any Fuzzy/Translated/Approved translations will be left as is.
     *
     * @param versionId
     *         version id
     * @param prefillRequest
     *         prefill configuration
     * @param taskHandle
     *         async task handle
     * @return future
     */
    Future<Void> prefillProjectVersionWithMachineTranslation(long versionId,
            MachineTranslationPrefill prefillRequest,
            @Nonnull MachineTranslationPrefillTaskHandle taskHandle);
}
