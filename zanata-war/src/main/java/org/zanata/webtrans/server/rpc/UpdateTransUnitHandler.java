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
package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.util.Event;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateTransUnit.class)
public class UpdateTransUnitHandler extends
        AbstractActionHandler<UpdateTransUnit, UpdateTransUnitResult> {
    @In(value = "webtrans.gwt.TransUnitUpdateHelper", create = true)
    private TransUnitUpdateHelper transUnitUpdateHelper;

    @In
    private TranslationService translationServiceImpl;

    @In
    private SecurityService securityServiceImpl;

    @In("event")
    private Event<TextFlowTargetUpdateContextEvent>
            textFlowTargetUpdateContextEvent;

    @Override
    public UpdateTransUnitResult execute(UpdateTransUnit action,
            ExecutionContext context) throws ActionException {

        Optional<TransUnitUpdateRequest> hasReviewUpdate =
                Iterables.tryFind(action.getUpdateRequests(),
                        new Predicate<TransUnitUpdateRequest>() {
                            @Override
                            public boolean apply(TransUnitUpdateRequest input) {
                                return input.getNewContentState().isReviewed();
                            }
                        });
        if (hasReviewUpdate.isPresent()) {
            securityServiceImpl.checkWorkspaceAction(action.getWorkspaceId(),
                    SecurityService.TranslationAction.REVIEW);
        } else {
            securityServiceImpl.checkWorkspaceAction(action.getWorkspaceId(),
                    SecurityService.TranslationAction.MODIFY);
        }

        return doTranslation(action.getWorkspaceId().getLocaleId(),
                action.getUpdateRequests(), action.getEditorClientId(),
                action.getUpdateType());
    }

    public UpdateTransUnitResult doTranslation(LocaleId localeId,
            List<TransUnitUpdateRequest> updateRequests,
            EditorClientId editorClientId,
            TransUnitUpdated.UpdateType updateType) {

        for (TransUnitUpdateRequest updateRequest : updateRequests) {
            textFlowTargetUpdateContextEvent.fire(
                    new TextFlowTargetUpdateContextEvent(updateRequest
                            .getTransUnitId(), localeId, editorClientId,
                            updateType));
        }

        List<TranslationResult> translationResults =
                translationServiceImpl.translate(localeId, updateRequests);
        return transUnitUpdateHelper
                .generateUpdateTransUnitResult(translationResults);
    }

    @Override
    public void rollback(UpdateTransUnit action, UpdateTransUnitResult result,
            ExecutionContext context) throws ActionException {
        // TODO implement rollback by checking result for success
        // if success, looking up base revision from action and set values back
        // to that
        // only if concurrent change conditions are satisfied
        // conditions: no new translations after this one

        // this should just use calls to a service to replace with previous
        // version
        // by version num (fail if previousVersion != latestVersion-1)
    }
}
