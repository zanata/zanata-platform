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
package org.zanata.service;

import javax.annotation.Nonnull;

import org.zanata.async.handle.CopyVersionTaskHandle;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

import java.util.concurrent.Future;

public interface CopyVersionService {
    //@formatter:off
    /**
     *  Perform copy of HProjectIteration in order
     *
     *  1) Copy version settings
     *  2) Copy HDocument (in batch)
     *  3) Copy textFlow for each of copied document (in batch)
     *  4) Copy textFlowTarget for each of copied textFlow (in batch)
     *
     * @param projectSlug
     * @param versionSlug
     * @param newVersionSlug
     *
     */
    //@formatter:on
    void copyVersion(String projectSlug, String versionSlug,
            String newVersionSlug, CopyVersionTaskHandle handle);

    //@formatter:off
    /**
     *  Starts a background version copy.
     *
     *  1) Copy version settings
     *  2) Copy HDocument (in batch)
     *  3) Copy textFlow for each of copied document (in batch)
     *  4) Copy textFlowTarget for each of copied textFlow (in batch)
     *
     * @param projectSlug
     * @param versionSlug
     * @param newVersionSlug
     *
     *
     */
    //@formatter:on
    Future<Void> startCopyVersion(String projectSlug, String versionSlug,
            String newVersionSlug, CopyVersionTaskHandle handle);

    /**
     * Return total count of HDocument in HProjectIteration
     *
     * @param projectSlug
     * @param versionSlug
     */
    int getTotalDocCount(String projectSlug,
            String versionSlug);

    /**
     * Create copy of HProjectIteration. Excludes properties: "slug", "project",
     * "children", "documents", "allDocuments"
     *
     * @param version
     * @param newVersion
     */
    HProjectIteration copyVersionSettings(
            HProjectIteration version, HProjectIteration newVersion);

    /**
     * Create copy of HDocument. Excludes properties: "projectIteration",
     * "poHeader", "poTargetHeaders", "rawDocument", "textFlows", "allTextFlows"
     *
     * @param newVersion
     * @param document
     */
    HDocument copyDocument(HProjectIteration newVersion, HDocument document)
            throws Exception;

    /**
     * Create copy of HRawDocument. Excludes properties: "document"
     *
     * @param newDocument
     * @param rawDocument
     */
    HRawDocument copyRawDocument(HDocument newDocument,
            @Nonnull HRawDocument rawDocument) throws Exception;

    /**
     * Create copy of HTextFlow. Excludes properties: "document", "targets",
     * "history", "potEntryData"
     *
     * @param newDocument
     * @param textFlow
     */
    HTextFlow copyTextFlow(HDocument newDocument, HTextFlow textFlow)
            throws Exception;

    /**
     * Create copy of HTextFlowTarget with all histories. Excludes properties:
     * "textFlow", "reviewComments", "history"
     *
     * @param textFlow
     * @param tft
     */
    HTextFlowTarget copyTextFlowTarget(HTextFlow textFlow,
            HTextFlowTarget tft) throws Exception;
}
