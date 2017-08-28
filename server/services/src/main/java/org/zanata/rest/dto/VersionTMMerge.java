/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.dto;


import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.rest.dto.HasTMMergeCriteria;
import org.zanata.webtrans.shared.rest.dto.InternalTMSource;
import org.zanata.webtrans.shared.rpc.MergeRule;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class VersionTMMerge implements HasTMMergeCriteria {
    private LocaleId localeId;
    private int thresholdPercent;
    private MergeRule differentProjectRule;
    private MergeRule differentDocumentRule;
    private MergeRule differentContextRule;
    private MergeRule importedMatchRule;
    private InternalTMSource internalTMSource;

    public VersionTMMerge(LocaleId localeId, int thresholdPercent,
            MergeRule differentProjectRule, MergeRule differentDocumentRule,
            MergeRule differentContextRule,
            MergeRule importedMatchRule,
            InternalTMSource internalTMSource) {
        this.localeId = localeId;
        this.thresholdPercent = thresholdPercent;
        this.differentDocumentRule = differentDocumentRule;
        this.differentContextRule = differentContextRule;
        this.importedMatchRule = importedMatchRule;
        this.internalTMSource = internalTMSource;
        this.differentProjectRule = differentProjectRule;
    }

    public VersionTMMerge() {
    }

    public LocaleId getLocaleId() {
        return localeId;
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
        return internalTMSource;
    }

}
