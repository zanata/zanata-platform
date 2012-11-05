/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import java.util.Collection;
import java.util.Map;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.SessionEventData;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public interface TranslationWorkspace
{
   public Map<EditorClientId, PersonSessionDetails> getUsers();
   void addEditorClient(String httpSessionId, EditorClientId editorClientId, PersonId personId);
   boolean removeEditorClient(EditorClientId editorClientId);

   Collection<EditorClientId> removeEditorClients(String httpSessionId);
   <T extends SessionEventData> void publish(T eventData);
   WorkspaceContext getWorkspaceContext();
   void updateUserSelection(EditorClientId editorClientId, TransUnitId selectedTransUnitId);
   TransUnitId getUserSelection(EditorClientId editorClientId);
   public void onEventServiceConnected(EditorClientId editorClientId, String connectionId);
}