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

package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.TranslatorStatusUpdate;
import org.zanata.webtrans.shared.rpc.TranslatorStatusUpdateAction;
import org.zanata.webtrans.shared.rpc.TranslatorUpdateStatusResult;

@Name("webtrans.gwt.TranslatorStatusUpdateHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(TranslatorStatusUpdateAction.class)
public class TranslatorStatusUpdateHandler extends AbstractActionHandler<TranslatorStatusUpdateAction, TranslatorUpdateStatusResult>
{
   @In
   private TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public TranslatorUpdateStatusResult execute(TranslatorStatusUpdateAction action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());
      workspace.updateUserSelection(action.getSessionId(), action.getSelectedTransUnit());
      // Send TranslatorStatusUpdate event to client
      TranslatorStatusUpdate event = new TranslatorStatusUpdate(action.getSessionId(), action.getPerson(), action.getSelectedTransUnit());
      workspace.publish(event);

      return new TranslatorUpdateStatusResult();
   }

   @Override
   public void rollback(TranslatorStatusUpdateAction action, TranslatorUpdateStatusResult result, ExecutionContext context) throws ActionException
   {
   }


}
