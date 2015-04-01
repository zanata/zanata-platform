/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.server.rpc;

import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.util.Event;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.UpdateType;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

/**
 * @author David Mason, damason@redhat.com
 *
 * @see RevertTransUnitUpdates
 */
@Name("webtrans.gwt.RevertTransUnitUpdatesHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(RevertTransUnitUpdates.class)
public class RevertTransUnitUpdatesHandler extends
        AbstractActionHandler<RevertTransUnitUpdates, UpdateTransUnitResult> {
    @In
    private TranslationService translationServiceImpl;

    @In
    private SecurityService securityServiceImpl;

    @In(value = "webtrans.gwt.TransUnitUpdateHelper", create = true)
    private TransUnitUpdateHelper transUnitUpdateHelper;

    @In("event")
    private Event<TextFlowTargetUpdateContextEvent> textFlowTargetUpdateContextEvent;

    @Override
    public UpdateTransUnitResult execute(RevertTransUnitUpdates action,
            ExecutionContext context) throws ActionException {
        securityServiceImpl.checkWorkspaceAction(action.getWorkspaceId(),
                SecurityService.TranslationAction.MODIFY);

        if (Events.exists()) {
            for (TransUnitUpdateInfo updateInfo : action.getUpdatesToRevert()) {
                textFlowTargetUpdateContextEvent.fire(
                        new TextFlowTargetUpdateContextEvent(
                                updateInfo.getTransUnit().getId(),
                                action.getWorkspaceId().getLocaleId(),
                                action.getEditorClientId(),
                                UpdateType.NonEditorSave));
            }
        }

        List<TranslationResult> revertResults =
                translationServiceImpl.revertTranslations(action
                        .getWorkspaceId().getLocaleId(), action
                        .getUpdatesToRevert());

        return transUnitUpdateHelper
                .generateUpdateTransUnitResult(revertResults);
    }

    @Override
    public void rollback(RevertTransUnitUpdates action,
            UpdateTransUnitResult result, ExecutionContext context)
            throws ActionException {
    }
}
