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
package org.zanata.webtrans.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.events.TransMemoryMergeEvent;
import org.zanata.events.TransMemoryMergeProgressEvent;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.TMMergeInProgress;
import org.zanata.webtrans.shared.rpc.TransMemoryMergeStartOrEnd;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class TransMemoryMergeEventPublisher {
    private static final Logger log =
            LoggerFactory.getLogger(TransMemoryMergeEventPublisher.class);
    @Inject
    private TranslationWorkspaceManager translationWorkspaceManager;


    public void onTMMergeEvent(@Observes TransMemoryMergeEvent event) {
        WorkspaceId workspaceId = event.getWorkspaceId();
        try {
            TranslationWorkspace workspace =
                    translationWorkspaceManager.getOrRegisterWorkspace(
                            workspaceId);
            TransMemoryMergeStartOrEnd clientEvent =
                    new TransMemoryMergeStartOrEnd(event.getStartTime(),
                            event.getUsername(),
                            event.getEditorClientId(), event.getDocumentId(),
                            event.getTotal(), event.getEndTime());
            workspace.publish(clientEvent);

        } catch (NoSuchWorkspaceException e) {
            log.info("no workspace for {}", workspaceId);
        }
    }

    public void onTMMergeProgress(@Observes TransMemoryMergeProgressEvent event) {
        WorkspaceId workspaceId = event.getWorkspaceId();
        try {
            TranslationWorkspace workspace =
                    translationWorkspaceManager.getOrRegisterWorkspace(
                            workspaceId);
            TMMergeInProgress clientEvent =
                    new TMMergeInProgress(event.getTotal(), event.getFilled(),
                            event.getEditorClientId(), event.getDocumentId());
            workspace.publish(clientEvent);

        } catch (NoSuchWorkspaceException e) {
            log.info("no workspace for {}", workspaceId);
        }
    }
}
