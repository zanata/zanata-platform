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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.test.GWTTestData;

import com.google.common.collect.MapMaker;

import static org.assertj.core.api.Assertions.assertThat;

public class TranslationWorkspaceImplTest {
    private TranslationWorkspaceImpl translationWorkspace;
    private WorkspaceId workspaceId;

    @Before
    public void setUp() {
        workspaceId =
                new WorkspaceId(ProjectIterationId.of("project", "master",
                        ProjectType.Podir), new LocaleId("en-US"));
        WorkspaceContext workspaceContext =
                new WorkspaceContext(workspaceId, "workspaceName", "en-US");
        translationWorkspace = new TranslationWorkspaceImpl(workspaceContext);
    }

    @Test(expected = NullPointerException.class)
    public void willNotCreateTranslationWorkspaceWithNullContext() {
        translationWorkspace = new TranslationWorkspaceImpl(null);
    }

    @Test
    public void canGetWorkspaceContext() {
        WorkspaceContext workspaceContext =
                translationWorkspace.getWorkspaceContext();

        assertThat(workspaceContext.getWorkspaceId()).isEqualTo(workspaceId);
    }

    @Test
    public void canGetUsers() {
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        PersonId personId = new PersonId("personId");
        translationWorkspace.addEditorClient("sessionId", editorClientId,
                personId);

        Map<EditorClientId, PersonSessionDetails> users =
                translationWorkspace.getUsers();

        assertThat(users).containsKey(editorClientId);
        PersonSessionDetails personSessionDetails = users.get(editorClientId);
        assertThat(personSessionDetails.getPerson().getId()).isEqualTo(personId);
    }

    @Test
    public void canRemoveClient() {
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        PersonId personId = new PersonId("personId");
        translationWorkspace.addEditorClient("sessionId", editorClientId,
                personId);
        assertThat(translationWorkspace.getUsers()).containsKey(editorClientId);

        translationWorkspace.removeEditorClient(editorClientId);

        assertThat(translationWorkspace.getUsers().size()).isEqualTo(0);
    }

    @Test
    public void canRemoveClientsWithSameSessionId() {
        // Given: 2 client share same http session id
        String httpSessionId = "sessionId";
        EditorClientId editorClientId1 = new EditorClientId(httpSessionId, 1);
        EditorClientId editorClientId2 = new EditorClientId(httpSessionId, 2);
        PersonId personId = new PersonId("personId");
        translationWorkspace.addEditorClient(httpSessionId, editorClientId1,
                personId);
        translationWorkspace.addEditorClient(httpSessionId, editorClientId2,
                personId);
        assertThat(translationWorkspace.getUsers().keySet())
                .contains(editorClientId1, editorClientId2);

        // When:
        translationWorkspace.removeEditorClients(httpSessionId);

        // Then:
        assertThat(translationWorkspace.getUsers().entrySet()).isEmpty();
    }

    @Test
    public void canUpdateUserSelection() {
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        translationWorkspace.addEditorClient("sessionId", editorClientId,
                new PersonId("personId"));
        TransUnit selectedTransUnit = GWTTestData.makeTransUnit((long) 1);

        translationWorkspace.updateUserSelection(editorClientId,
                selectedTransUnit.getId());

        PersonSessionDetails personSessionDetails =
                translationWorkspace.getUsers().get(editorClientId);
        assertThat(personSessionDetails.getSelectedTransUnitId())
                .isEqualTo(selectedTransUnit.getId());
        assertThat(translationWorkspace.getUserSelection(editorClientId))
                .isEqualTo(selectedTransUnit.getId());
    }

    @Test
    public void onTimeoutRemove() {
        ConcurrentMap<EditorClientId, PersonId> sessions =
                new MapMaker().makeMap();
        sessions.put(new EditorClientId("a", 1), new PersonId("person a"));
        sessions.put(new EditorClientId("b", 1), new PersonId("person b"));

        sessions.remove(new EditorClientId("a", 1));
    }
}
