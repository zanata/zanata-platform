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
package org.zanata.webtrans.shared.rest.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.MergeRule;

/**
 * TM Merge for an individual document
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergeRequest implements HasTMMergeCriteria {
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
            justification = "For future implement")
    public EditorClientId editorClientId;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
            justification = "For future implement")
    public ProjectIterationId projectIterationId;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
            justification = "For future implement")
    public DocumentId documentId;
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
            justification = "For future implement")
    public LocaleId localeId;
    public int thresholdPercent;
    public MergeRule differentProjectRule;
    public MergeRule differentDocumentRule;
    public MergeRule differentContextRule;
    public MergeRule importedMatchRule;

    public TransMemoryMergeRequest(
            EditorClientId editorClientId,
            ProjectIterationId projectIterationId,
            DocumentId documentId, LocaleId localeId, int thresholdPercent,
            MergeRule differentProjectRule,
            MergeRule differentDocumentRule,
            MergeRule differentContextRule,
            MergeRule importedMatchRule) {
        this.editorClientId = editorClientId;
        this.projectIterationId = projectIterationId;
        this.documentId = documentId;
        this.localeId = localeId;
        this.thresholdPercent = thresholdPercent;
        this.differentProjectRule = differentProjectRule;
        this.differentDocumentRule = differentDocumentRule;
        this.differentContextRule = differentContextRule;
        this.importedMatchRule = importedMatchRule;
    }

    public TransMemoryMergeRequest() {
    }

    @Override
    public int getThresholdPercent() {
        return thresholdPercent;
    }

    @Override
    public MergeRule getDifferentProjectRule() {
        return differentProjectRule;
    }

    @Override
    public MergeRule getDifferentDocumentRule() {
        return differentDocumentRule;
    }

    @Override
    public MergeRule getDifferentContextRule() {
        return differentContextRule;
    }

    @Override
    public MergeRule getImportedMatchRule() {
        return importedMatchRule;
    }

    @Override
    public InternalTMSource getInternalTMSource() {
        return InternalTMSource.SELECT_ALL;
    }

}
