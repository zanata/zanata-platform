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
package org.zanata.action;

import static org.zanata.async.AsyncTaskKey.joinFields;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.AsyncTaskKey;
import org.zanata.async.GenericAsyncTaskKey;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.i18n.Messages;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
// TODO This class should be merged with the copy trans service (?)

/**
 * Manager Bean that keeps track of manual copy trans being run in the system,
 * to avoid duplicates and to provide asynchronous feedback.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Dependent
public class CopyTransManager implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    private CopyTransService copyTransServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private Messages messages;

    public boolean isCopyTransRunning(@Nonnull Object target) {
        AsyncTaskKey key;
        if (target instanceof HProjectIteration) {
            key = CopyTransProcessKey.getKey((HProjectIteration) target);
        } else if (target instanceof HDocument) {
            key = CopyTransProcessKey.getKey((HDocument) target);
        } else {
            throw new IllegalArgumentException(
                    "Copy Trans can only run for HProjectIteration and HDocument");
        }
        AsyncTaskHandle<?> handle = asyncTaskHandleManager.getHandleByKey(key);
        return handle != null && !handle.isDone();
    }

    /**
     * Start a Translation copy with the default options.
     */
    public void startCopyTrans(HProjectIteration iteration) {
        this.startCopyTrans(iteration, new HCopyTransOptions());
    }

    /**
     * Start a Translation copy for a document with the given options.
     *
     * @param document
     *            The document for which to start copy trans.
     * @param options
     *            The options to run copy trans with.
     */
    public void startCopyTrans(HDocument document, HCopyTransOptions options) {
        if (isCopyTransRunning(document)) {
            throw new RuntimeException(
                    "Copy Trans is already running for document \'"
                            + document.getDocId() + "\'");
        }
        AsyncTaskKey key = CopyTransProcessKey.getKey(document);
        CopyTransTaskHandle handle = new CopyTransTaskHandle();
        handle.setTaskName(messages.format("jsf.tasks.copyTranslationsDoc",
                document.getDocId()));
        handle.setTriggeredBy(identity.getAccountUsername());
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        copyTransServiceImpl.startCopyTransForDocument(document, options,
                handle);
    }

    /**
     * Start a Translation copy with the given custom options.
     */
    public void startCopyTrans(HProjectIteration iteration,
            HCopyTransOptions options) {
        // double check
        if (isCopyTransRunning(iteration)) {
            throw new RuntimeException(
                    "Copy Trans is already running for version \'"
                            + iteration.getSlug() + "\'");
        }
        AsyncTaskKey key = CopyTransProcessKey.getKey(iteration);
        CopyTransTaskHandle handle = new CopyTransTaskHandle();
        handle.setTriggeredBy(identity.getAccountUsername());
        handle.setTaskName(
                messages.format("jsf.tasks.copyTranslationsVersion",
                iteration.getProject().getSlug(), iteration.getSlug()));
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        copyTransServiceImpl.startCopyTransForIteration(iteration, options,
                handle);
    }

    public CopyTransTaskHandle
            getCopyTransProcessHandle(@Nonnull Object target) {
        AsyncTaskKey key;
        if (target instanceof HProjectIteration) {
            key = CopyTransProcessKey.getKey((HProjectIteration) target);
        } else if (target instanceof HDocument) {
            key = CopyTransProcessKey.getKey((HDocument) target);
        } else {
            throw new IllegalArgumentException(
                    "Copy Trans can only run for HProjectIteration and HDocument");
        }
        return (CopyTransTaskHandle) asyncTaskHandleManager.getHandleByKey(key);
    }

    public void cancelCopyTrans(@Nonnull HProjectIteration iteration) {
        if (isCopyTransRunning(iteration)) {
            CopyTransTaskHandle handle =
                    this.getCopyTransProcessHandle(iteration);
            handle.cancel(true);
            handle.setCancelledTime(System.currentTimeMillis());
            handle.setCancelledBy(identity.getCredentials().getUsername());
        }
    }

    /**
     * Internal class to index Copy Trans processes.
     */
    private static final class CopyTransProcessKey {
        private static final String KEY_NAME = "copyTransKey";


        public static AsyncTaskKey getKey(HProjectIteration iteration) {
            return new GenericAsyncTaskKey(joinFields(iteration.getProject().getSlug(),
                    iteration.getSlug(), null));
        }

        public static AsyncTaskKey getKey(HDocument document) {
            String projectSlug = document.getProjectIteration().getProject().getSlug();
            String versionSlug = document.getProjectIteration().getSlug();
            String docId = document.getDocId();
            return new GenericAsyncTaskKey(joinFields(KEY_NAME, projectSlug, versionSlug, docId));
        }

    }
}
