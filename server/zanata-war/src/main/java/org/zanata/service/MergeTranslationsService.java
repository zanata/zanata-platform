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
package org.zanata.service;

import java.util.concurrent.Future;

import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.model.HProjectIteration;

public interface MergeTranslationsService {
    //@formatter:off
    /**
     *  Starts a background merge translations of a version to another.
     *
     * @param sourceProjectSlug - source project identifier
     * @param sourceVersionSlug - source version identifier
     * @param targetProjectSlug - target project identifier
     * @param targetVersionSlug - target version identifier
     * @param useNewerTranslation - to override translated/approved string in target with newer entry in source
     * @param handle - task handler for merge translation
     */
    //@formatter:on
    Future<Void> startMergeTranslations(String sourceProjectSlug,
        String sourceVersionSlug, String targetProjectSlug,
        String targetVersionSlug, boolean useNewerTranslation,
        MergeTranslationsTaskHandle handle);

    /**
     * Return total count of translations to be processed.
     *
     * @param sourceVersionId - source HProjectIteration id
     * @param targetVersionId - target HProjectIteration id
     */
    int getTotalProgressCount(HProjectIteration sourceVersion,
        HProjectIteration targetVersion);
}
