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

package org.zanata.model;

import java.util.Date;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.model.WorkspaceRestrictions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class TestFixture {
    public static TransUnit makeTransUnit(long id) {
        return makeTransUnit(id, ContentState.New);
    }

    public static TransUnit makeTransUnit(long id, ContentState contentState,
            String... targetContent) {
        return TransUnit.Builder.newTransUnitBuilder().setId(id)
                .setResId("resId" + id).setVerNum(0).setLocaleId("en")
                .addSource("source").addTargets(targetContent)
                .setStatus(contentState).setRowIndex((int) id).build();
    }

    public static TransUnit makeTransUnit(long id, ContentState contentState) {
        return makeTransUnit(id, contentState, "target");
    }

    public static UserWorkspaceContext userWorkspaceContext() {
        return userWorkspaceContext(true, true);
    }

    public static UserWorkspaceContext userWorkspaceContext(
            boolean projectActive, boolean hasWriteAccess, String projectSlug,
            String iterationSlug, ProjectType projectType) {
        ProjectIterationId projectIterationId =
                new ProjectIterationId(projectSlug, iterationSlug, projectType);
        WorkspaceRestrictions workspaceRestrictions =
                new WorkspaceRestrictions(projectActive, false, hasWriteAccess, true,
                        true);
        return new UserWorkspaceContext(new WorkspaceContext(new WorkspaceId(
                projectIterationId, LocaleId.EN_US), "workspaceName",
                LocaleId.EN_US.getId()), workspaceRestrictions);
    }

    public static UserWorkspaceContext userWorkspaceContext(
            boolean projectActive, boolean hasWriteAccess) {
        WorkspaceRestrictions workspaceRestrictions =
                new WorkspaceRestrictions(projectActive, false, hasWriteAccess, true,
                        true);
        return new UserWorkspaceContext(new WorkspaceContext(workspaceId(),
                "workspaceName", LocaleId.EN_US.getId()), workspaceRestrictions);
    }

    public static WorkspaceId workspaceId() {
        return workspaceId(LocaleId.EN_US);
    }

    public static WorkspaceId workspaceId(LocaleId localeId) {
        return workspaceId(localeId, "project", "master", ProjectType.Podir);
    }

    public static WorkspaceId workspaceId(LocaleId localeId,
            String projectSlug, String iterationSlug, ProjectType projectType) {
        return new WorkspaceId(new ProjectIterationId(projectSlug,
                iterationSlug, projectType), localeId);
    }

    public static Person person() {
        return new Person(new PersonId("pid"), "admin", null);
    }


    public static List<Integer> asIds(List<TransUnit> transUnits) {
        return Lists.newArrayList(Collections2.transform(transUnits,
                from -> (int) from.getId().getId()));
    }

    public static <T extends ModelEntityBase> T setId(Long id, T object) {
        object.setId(id);
        return object;
    }

    public static DocumentInfo documentInfo(long id, String docId) {
        return documentInfo(id, "", docId);
    }

    public static DocumentInfo documentInfo(long id, String path, String name) {
        return new DocumentInfo(new DocumentId(id, path + name), name, path,
                LocaleId.EN_US, null, new AuditInfo(new Date(), "Translator"),
                null, new AuditInfo(new Date(), "last translator"));
    }
}
