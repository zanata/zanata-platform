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
package org.zanata.webtrans.server;

import java.util.concurrent.ConcurrentMap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;

import com.google.common.collect.MapMaker;

@Test(groups = { "unit-tests" })
public class TranslationWorkspaceImplTest
{
   private TranslationWorkspaceImpl translationWorkspace;
   private WorkspaceId workspaceId;

   @BeforeMethod
   public void setUp()
   {
      workspaceId = new WorkspaceId(ProjectIterationId.of("project", "master"), new LocaleId("en-US"));
      WorkspaceContext workspaceContext = new WorkspaceContext(workspaceId, "workspaceName", "en-US");
      translationWorkspace = new TranslationWorkspaceImpl(workspaceContext);
   }

   @Test
   public void onTimeoutRemove()
   {
      ConcurrentMap<EditorClientId, PersonId> sessions = new MapMaker().makeMap();
      sessions.put(new EditorClientId("a", 1), new PersonId("person a"));
      sessions.put(new EditorClientId("b", 1), new PersonId("person b"));

      sessions.remove(new EditorClientId("a", 1));
   }
}
