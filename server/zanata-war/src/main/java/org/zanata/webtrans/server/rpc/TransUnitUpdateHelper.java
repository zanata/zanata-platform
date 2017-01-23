/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 *
 */
package org.zanata.webtrans.server.rpc;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.zanata.events.TextFlowTargetUpdatedEvent;
import org.zanata.model.*;
import org.zanata.service.*;
import org.zanata.util.IServiceLocator;
import org.zanata.webtrans.shared.model.*;
import org.zanata.webtrans.shared.rpc.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("webtrans.gwt.TransUnitUpdateHelper")
@javax.enterprise.context.ApplicationScoped
public class TransUnitUpdateHelper {

    @Inject
    private IServiceLocator serviceLocator;
    private static Cache<CacheKey, TransUnitUpdateInfo> cache = CacheBuilder
            .newBuilder().expireAfterAccess(1, TimeUnit.MILLISECONDS)
            .softValues().maximumSize(100).build();

    public void onTargetUpdatedSuccessful(@Observes(
            during = TransactionPhase.AFTER_SUCCESS) TextFlowTargetUpdatedEvent event) {
        TransUnitUpdated transUnitUpdated = event.getTransUnitUpdated();
        event.getWorkspace().publish(transUnitUpdated);
        TransUnit transUnit = transUnitUpdated.getUpdateInfo().getTransUnit();
        cache.put(
                new CacheKey(event.getTextFlowTargetId(),
                        transUnit.getVerNum()),
                transUnitUpdated.getUpdateInfo());
    }

    public UpdateTransUnitResult generateUpdateTransUnitResult(
            List<TranslationService.TranslationResult> translationResults) {
        TransUnitTransformer transUnitTransformer =
                serviceLocator.getInstance(TransUnitTransformer.class);
        UpdateTransUnitResult result = new UpdateTransUnitResult();
        for (TranslationService.TranslationResult translationResult : translationResults) {
            translationResult.getTranslatedTextFlowTarget().getId();
            HTextFlowTarget newTarget =
                    translationResult.getTranslatedTextFlowTarget();
            TransUnitUpdateInfo transUnitUpdateInfo = cache.getIfPresent(
                    new CacheKey(newTarget.getId(), newTarget.getVersionNum()));
            if (transUnitUpdateInfo != null) {
                // All these information is gathered in
                // TranslationUpdateListener.
                result.addUpdateResult(transUnitUpdateInfo);
            } else {
                HTextFlow hTextFlow = newTarget.getTextFlow();
                int wordCount = hTextFlow.getWordCount().intValue();
                TransUnit tu = transUnitTransformer.transform(hTextFlow,
                        newTarget.getLocale());
                TransUnitUpdateInfo updateInfo = build(translationResult,
                        new DocumentId(hTextFlow.getDocument().getId(),
                                hTextFlow.getDocument().getDocId()),
                        tu, wordCount);
                result.addUpdateResult(updateInfo);
            }
        }
        return result;
    }

    private static TransUnitUpdateInfo build(
            TranslationService.TranslationResult translationResult,
            DocumentId documentId, TransUnit transUnit, int wordCount) {
        return new TransUnitUpdateInfo(
                translationResult.isTranslationSuccessful(),
                translationResult.isTargetChanged(), documentId, transUnit,
                wordCount, translationResult.getBaseVersionNum(),
                translationResult.getBaseContentState(),
                translationResult.getErrorMessage());
    }

    private static class CacheKey {
        private final Long textFlowTargetId;
        private final Integer versionNum;

        @java.beans.ConstructorProperties({ "textFlowTargetId", "versionNum" })
        public CacheKey(final Long textFlowTargetId, final Integer versionNum) {
            this.textFlowTargetId = textFlowTargetId;
            this.versionNum = versionNum;
        }
    }
}
