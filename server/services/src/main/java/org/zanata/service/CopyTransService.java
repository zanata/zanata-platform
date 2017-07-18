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

import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.concurrent.Future;

public interface CopyTransService extends Serializable {
    /**
     * Copies previous matching translations for all available locales into a
     * document. Translations are matching if their document id, textflow id and
     * source content are identical, and their state is approved.
     *
     * The text flow revision for copied targets is set to the current text flow
     * revision.
     *
     * This method will use the default Copy Trans options for the document's
     * project. If not set, it will use the default global options.
     *
     * @param document
     *            the document to copy translations into
     */
    void copyTransForDocument(HDocument document, CopyTransTaskHandle handle);

    /**
     *
     * @param document
     *            the document to copy translations into
     * @param copyTransOptions
     *            The copy Trans options to use.
     * @param handle
     *            Optional Task handle to track progress for the operation.
     */
    Future<Void> startCopyTransForDocument(HDocument document,
            HCopyTransOptions copyTransOptions, CopyTransTaskHandle handle);

    /**
     *
     *
     * @param iteration
     *            The project iteration to copy translations into
     * @param copyTransOptions
     *            The copy Trans options to use.
     * @param handle Task handle to track progress for the operation.
     */
    Future<Void> startCopyTransForIteration(HProjectIteration iteration,
            HCopyTransOptions copyTransOptions,
            @Nonnull CopyTransTaskHandle handle);

}
