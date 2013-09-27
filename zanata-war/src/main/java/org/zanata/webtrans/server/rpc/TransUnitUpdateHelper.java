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

import org.jboss.seam.*;
import org.jboss.seam.annotations.*;
import org.zanata.model.*;
import org.zanata.service.*;
import org.zanata.webtrans.server.*;
import org.zanata.webtrans.shared.auth.*;
import org.zanata.webtrans.shared.model.*;
import org.zanata.webtrans.shared.rpc.*;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("webtrans.gwt.TransUnitUpdateHelper")
@Scope(ScopeType.STATELESS)
public class TransUnitUpdateHelper {
    @In
    private TransUnitTransformer transUnitTransformer;

    public UpdateTransUnitResult generateUpdateTransUnitResult(
            List<TranslationService.TranslationResult> translationResults,
            EditorClientId editorClientId,
            TransUnitUpdated.UpdateType updateType,
            TranslationWorkspace workspace) {
        UpdateTransUnitResult result = new UpdateTransUnitResult();

        for (TranslationService.TranslationResult translationResult : translationResults) {
            HTextFlowTarget newTarget =
                    translationResult.getTranslatedTextFlowTarget();
            HTextFlow hTextFlow = newTarget.getTextFlow();
            int wordCount = hTextFlow.getWordCount().intValue();
            TransUnit tu =
                    transUnitTransformer.transform(hTextFlow,
                            newTarget.getLocale());
            TransUnitUpdateInfo updateInfo =
                    build(translationResult, new DocumentId(hTextFlow
                            .getDocument().getId(), hTextFlow.getDocument()
                            .getDocId()), tu, wordCount);

            workspace.publish(new TransUnitUpdated(updateInfo, editorClientId,
                    updateType));

            result.addUpdateResult(updateInfo);
        }
        return result;
    }

    private TransUnitUpdateInfo build(
            TranslationService.TranslationResult translationResult,
            DocumentId documentId, TransUnit transUnit, int wordCount) {
        return new TransUnitUpdateInfo(
                translationResult.isTranslationSuccessful(),
                translationResult.isTargetChanged(), documentId, transUnit,
                wordCount, translationResult.getBaseVersionNum(),
                translationResult.getBaseContentState(),
                translationResult.getErrorMessage());
    }
}
