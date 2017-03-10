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

import java.io.Serializable;
import javax.annotation.Nonnull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;
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
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CopyTransManager.class);

    private static final long serialVersionUID = 1L;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    private CopyTransService copyTransServiceImpl;
    @Inject
    private ZanataIdentity identity;

    public boolean isCopyTransRunning(@Nonnull Object target) {
        CopyTransProcessKey key;
        if (target instanceof HProjectIteration) {
            key = CopyTransProcessKey.getKey((HProjectIteration) target);
        } else if (target instanceof HDocument) {
            key = CopyTransProcessKey.getKey((HDocument) target);
        } else {
            throw new IllegalArgumentException(
                    "Copy Trans can only run for HProjectIteration and HDocument");
        }
        AsyncTaskHandle handle = asyncTaskHandleManager.getHandleByKey(key);
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
        CopyTransProcessKey key = CopyTransProcessKey.getKey(document);
        CopyTransTaskHandle handle = new CopyTransTaskHandle();
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
        CopyTransProcessKey key = CopyTransProcessKey.getKey(iteration);
        CopyTransTaskHandle handle = new CopyTransTaskHandle();
        asyncTaskHandleManager.registerTaskHandle(handle, key);
        copyTransServiceImpl.startCopyTransForIteration(iteration, options,
                handle);
    }

    public CopyTransTaskHandle
            getCopyTransProcessHandle(@Nonnull Object target) {
        CopyTransProcessKey key;
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
            CopyTransProcessKey key = CopyTransProcessKey.getKey(iteration);
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
    private static final class CopyTransProcessKey implements Serializable {
        private static final long serialVersionUID = -2054359069473618887L;
        private String projectSlug;
        private String iterationSlug;
        private String docId;

        public static CopyTransProcessKey getKey(HProjectIteration iteration) {
            CopyTransProcessKey newKey = new CopyTransProcessKey();
            newKey.setProjectSlug(iteration.getProject().getSlug());
            newKey.setIterationSlug(iteration.getSlug());
            return newKey;
        }

        public static CopyTransProcessKey getKey(HDocument document) {
            CopyTransProcessKey newKey = new CopyTransProcessKey();
            newKey.setDocId(document.getDocId());
            newKey.setProjectSlug(
                    document.getProjectIteration().getProject().getSlug());
            newKey.setIterationSlug(document.getProjectIteration().getSlug());
            return newKey;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof CopyTransManager.CopyTransProcessKey))
                return false;
            final CopyTransProcessKey other = (CopyTransProcessKey) o;
            final Object this$projectSlug = this.getProjectSlug();
            final Object other$projectSlug = other.getProjectSlug();
            if (this$projectSlug == null ? other$projectSlug != null
                    : !this$projectSlug.equals(other$projectSlug))
                return false;
            final Object this$iterationSlug = this.getIterationSlug();
            final Object other$iterationSlug = other.getIterationSlug();
            if (this$iterationSlug == null ? other$iterationSlug != null
                    : !this$iterationSlug.equals(other$iterationSlug))
                return false;
            final Object this$docId = this.getDocId();
            final Object other$docId = other.getDocId();
            if (this$docId == null ? other$docId != null
                    : !this$docId.equals(other$docId))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $projectSlug = this.getProjectSlug();
            result = result * PRIME
                    + ($projectSlug == null ? 43 : $projectSlug.hashCode());
            final Object $iterationSlug = this.getIterationSlug();
            result = result * PRIME
                    + ($iterationSlug == null ? 43 : $iterationSlug.hashCode());
            final Object $docId = this.getDocId();
            result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
            return result;
        }

        public String getProjectSlug() {
            return this.projectSlug;
        }

        public String getIterationSlug() {
            return this.iterationSlug;
        }

        public String getDocId() {
            return this.docId;
        }

        public void setProjectSlug(final String projectSlug) {
            this.projectSlug = projectSlug;
        }

        public void setIterationSlug(final String iterationSlug) {
            this.iterationSlug = iterationSlug;
        }

        public void setDocId(final String docId) {
            this.docId = docId;
        }

        private CopyTransProcessKey() {
        }
    }
}
