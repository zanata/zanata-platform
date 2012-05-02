/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.server;

import java.util.concurrent.ConcurrentMap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.SessionId;
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
      WorkspaceContext workspaceContext = new WorkspaceContext(workspaceId, "workspaceName", "en-US", false);
      translationWorkspace = new TranslationWorkspaceImpl(workspaceContext);
   }

   @Test
   public void onTimeoutRemove()
   {
      ConcurrentMap<SessionId, PersonId> sessions = new MapMaker().makeMap();
      sessions.put(SessionId.of("a"), new PersonId("person a"));
      sessions.put(SessionId.of("b"), new PersonId("person b"));

      sessions.remove(SessionId.of("a"));
   }
}
