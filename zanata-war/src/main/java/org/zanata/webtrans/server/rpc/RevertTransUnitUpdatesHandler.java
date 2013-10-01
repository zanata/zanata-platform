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

import java.util.*;

import org.jboss.seam.*;
import org.jboss.seam.annotations.*;
import org.zanata.model.*;
import org.zanata.service.*;
import org.zanata.service.TranslationService.*;
import org.zanata.webtrans.server.*;
import org.zanata.webtrans.shared.rpc.*;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.*;

import net.customware.gwt.dispatch.server.*;
import net.customware.gwt.dispatch.shared.*;

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

    @Override
    public UpdateTransUnitResult execute(RevertTransUnitUpdates action,
            ExecutionContext context) throws ActionException {
        SecurityService.SecurityCheckResult securityCheckResult =
                securityServiceImpl.checkPermission(action,
                        SecurityService.TranslationAction.MODIFY);
        HLocale hLocale = securityCheckResult.getLocale();
        TranslationWorkspace workspace = securityCheckResult.getWorkspace();

        List<TranslationResult> revertResults =
                translationServiceImpl.revertTranslations(
                        hLocale.getLocaleId(), action.getUpdatesToRevert());

        return transUnitUpdateHelper.generateUpdateTransUnitResult(
                revertResults, action.getEditorClientId(), UpdateType.Revert,
                workspace);
    }

    @Override
    public void rollback(RevertTransUnitUpdates action,
            UpdateTransUnitResult result, ExecutionContext context)
            throws ActionException {
    }
}
