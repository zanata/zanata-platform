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
package org.zanata.service;

import org.zanata.async.AsyncTaskHandle;
import org.zanata.events.DocStatsEvent;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.resource.Resource;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface DocumentService extends Serializable {
    // milestone for contentState to publish event (percentage)
    public static final int DOC_EVENT_MILESTONE= 100;

    /**
     * Creates or Updates a document.
     *
     * @param projectSlug
     *            The document's project id.
     * @param iterationSlug
     *            The document's project iteration id.
     * @param sourceDoc
     *            The document to save. (If the document's name matches a docId
     *            already stored, it will be overwritten)
     * @param extensions
     *            Document extensions to save.
     * @param copyTrans
     *            Whether to copy translations from other projects or not. A
     *            true value does not guarantee that this will happen, it is
     *            only a suggestion.
     * @param lock
     *            If true, no other document save will be allowed for the same
     *            document until this invocation has finished.
     * @return The created / updated document
     */
    public HDocument saveDocument(String projectSlug, String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans,
            boolean lock);

    /**
     * Creates or Updates a document.
     *
     * @param projectSlug
     *            The document's project id.
     * @param iterationSlug
     *            The document's project iteration id.
     * @param sourceDoc
     *            The document to save. (If the document's name matches a docId
     *            already stored, it will be overwritten)
     * @param extensions
     *            Document extensions to save.
     * @param copyTrans
     *            Whether to copy translations from other projects or not. A
     *            true value does not guarantee that this will happen, it is
     *            only a suggestion.
     * @return The created / updated document
     */
    public HDocument saveDocument(String projectSlug, String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans);

    /**
     * Creates or updates a document asynchronously. The process will be started
     * in a different thread.
     *
     * @param projectSlug
     * @param iterationSlug
     * @param sourceDoc
     * @param extensions
     * @param copyTrans
     * @param lock
     * @return A future object that will eventually contain the result of the
     * document save.
     * @see {@link org.zanata.service.DocumentService#saveDocument(String, String, org.zanata.rest.dto.resource.Resource, java.util.Set, boolean, boolean)}
     */
    CompletableFuture<HDocument> saveDocumentAsync(String projectSlug,
            String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans,
            boolean lock, AsyncTaskHandle<HDocument> handle);

    /**
     * Makes a document obsolete.
     *
     * @param document
     *            The document to make obsolete.
     */
    public void makeObsolete(HDocument document);

    /**
     * Post process when statistic in document changes
     * (on DocStatsEvent)
     *
     * @param event
     */
    public void documentStatisticUpdated(DocStatsEvent event);
}
