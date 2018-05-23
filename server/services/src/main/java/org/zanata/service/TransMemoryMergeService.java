/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import com.google.common.annotations.VisibleForTesting;

public interface TransMemoryMergeService extends Serializable {

    @VisibleForTesting
    int BATCH_SIZE = 50;

    /**
     * TM merge for a single document
     * @param request
     * @param asyncTaskHandle
     * @return
     */
    List<TranslationService.TranslationResult> executeMerge(
            TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle);

    /**
     * TM merge for a single document
     * @param request
     * @param asyncTaskHandle
     * @return
     */
    Future<List<TranslationService.TranslationResult>> executeMergeAsync(TransMemoryMergeRequest request,
            TransMemoryMergeTaskHandle asyncTaskHandle);

    /**
     * TM merge for a project version
     * @param versionId
     * @param mergeRequest
     * @param handle
     * @return
     */
    Future<Void> startMergeTranslations(Long versionId,
            VersionTMMerge mergeRequest,
            MergeTranslationsTaskHandle handle);
}
