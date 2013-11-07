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

import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChat;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;

@Name("webtrans.gwt.PublishWorkspaceChatHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(PublishWorkspaceChatAction.class)
public class PublishWorkspaceChatHandler extends
        AbstractActionHandler<PublishWorkspaceChatAction, NoOpResult> {
    @In
    private ZanataIdentity identity;

    @In
    private TranslationWorkspaceManager translationWorkspaceManager;

    @Override
    public NoOpResult execute(PublishWorkspaceChatAction action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();

        TranslationWorkspace workspace =
                translationWorkspaceManager.getOrRegisterWorkspace(action
                        .getWorkspaceId());
        // Send PublishWorkspaceChat event to client

        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy h:mm:ss");

        PublishWorkspaceChat event =
                new PublishWorkspaceChat(action.getPerson(),
                        formatter.format(currentDate), action.getMsg(),
                        action.getMessageType());
        workspace.publish(event);

        return new NoOpResult();
    }

    @Override
    public void rollback(PublishWorkspaceChatAction action, NoOpResult result,
            ExecutionContext context) throws ActionException {
    }

}
