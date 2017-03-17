/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.async.handle;

import org.zanata.async.AsyncTaskHandle;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;

import com.google.common.base.MoreObjects;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergeTaskHandle extends AsyncTaskHandle<Void> {
    private long textFlowFilled;
    private long totalTextFlows;
    private String cancelledBy;
    private Long cancelledTime;
    private String triggeredBy;
    private String mergeTarget;

    public long getTextFlowFilled() {
        return this.textFlowFilled;
    }

    public void setTextFlowFilled(final long textFlowFilled) {
        this.textFlowFilled = textFlowFilled;
    }

    public long getTotalTextFlows() {
        return this.totalTextFlows;
    }

    public void setTotalTextFlows(final long totalTextFlows) {
        this.totalTextFlows = totalTextFlows;
    }

    public String getCancelledBy() {
        return this.cancelledBy;
    }

    public void setCancelledBy(final String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public long getCancelledTime() {
        return this.cancelledTime;
    }

    public void setCancelledTime(final long cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public String getTriggeredBy() {
        return this.triggeredBy;
    }

    public void setTriggeredBy(final String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public void setTMMergeTarget(ProjectIterationId projectIterationId,
            DocumentId documentId, LocaleId localeId) {
        this.mergeTarget = String.format("%s - %s (%s)", projectIterationId,
                documentId.getDocId(), localeId);
    }

    public String getMergeTarget() {
        return mergeTarget;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("mergeTarget", mergeTarget)
                .add("textFlowFilled", textFlowFilled)
                .add("totalTextFlows", totalTextFlows)
                .add("cancelledBy", cancelledBy)
                .add("cancelledTime", cancelledTime)
                .add("triggeredBy", triggeredBy)
                .toString();
    }
}
