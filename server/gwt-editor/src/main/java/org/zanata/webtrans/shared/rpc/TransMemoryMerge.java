/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.shared.rpc;

import java.util.List;

import com.google.common.base.MoreObjects;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

import com.google.common.base.Objects;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMerge extends
        AbstractWorkspaceAction<UpdateTransUnitResult> {
    private static final long serialVersionUID = 1L;
    private int thresholdPercent;
    private List<TransUnitUpdateRequest> updateRequests;
    private MergeRule differentProjectRule;
    private MergeRule differentDocumentRule;
    private MergeRule differentContextRule;
    private MergeRule importedMatchRule;

    @SuppressWarnings("unused")
    TransMemoryMerge() {
    }

    public TransMemoryMerge(int threshold,
            List<TransUnitUpdateRequest> updateRequests,
            MergeOptions mergeOptions) {
        thresholdPercent = threshold;
        this.updateRequests = updateRequests;
        this.differentProjectRule = mergeOptions.getDifferentProject();
        this.differentDocumentRule = mergeOptions.getDifferentDocument();
        this.differentContextRule = mergeOptions.getDifferentResId();
        this.importedMatchRule = mergeOptions.getImportedMatch();
    }

    public int getThresholdPercent() {
        return thresholdPercent;
    }

    public MergeRule getDifferentProjectRule() {
        return differentProjectRule;
    }

    public MergeRule getDifferentDocumentRule() {
        return differentDocumentRule;
    }

    public MergeRule getDifferentContextRule() {
        return differentContextRule;
    }

    public MergeRule getImportedMatchRule() {
        return importedMatchRule;
    }

    public List<TransUnitUpdateRequest> getUpdateRequests() {
        return updateRequests;
    }

    @Override
    public String toString() {
        // @formatter:off
      return MoreObjects.toStringHelper(this).
            add("thresholdPercent", thresholdPercent).
            add("updateRequests", updateRequests).
            add("differentProjectRule", getDifferentProjectRule()).
            add("differentDocumentRule", getDifferentDocumentRule()).
            add("differentContextRule", getDifferentContextRule()).
            add("importedMatchRule", getImportedMatchRule()).
            toString();
      // @formatter:on
    }
}
