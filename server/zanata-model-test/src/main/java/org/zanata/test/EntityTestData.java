/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.test;

import java.lang.reflect.Method;
import java.util.Date;

import javax.annotation.Nonnull;

import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.ModelEntityBase;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemoryUnitVariant;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class EntityTestData {

    public static HTextFlow makeHTextFlow(long textFlowId, HLocale targetLocale,
            ContentState contentState) {
        return makeTextFlowInProject(textFlowId, new HLocale(LocaleId.EN_US), targetLocale, contentState, "pot/message.pot",
                "versionSlug", "projectSlug");
    }

    @Nonnull
    private static HTextFlow makeTextFlow(HDocument hDocument,
            long textFlowId) {
        HTextFlow hTextFlow =
                new HTextFlow(hDocument, "resId" + textFlowId);
        hTextFlow.setContents("hello world " + textFlowId);
        hTextFlow.setId(textFlowId);
        hTextFlow.setPos((int) textFlowId);
        return hTextFlow;
    }

    public static HTextFlow makeHTextFlow(long id, HLocale sourceLocale,
            HLocale targetLocale, ContentState contentState, String docId,
            String versionSlug, String projectSlug) {
        HProject hProject = new HProject();
        hProject.setSlug(projectSlug);

        HProjectIteration hProjectIteration = new HProjectIteration();
        hProjectIteration.setSlug(versionSlug);
        hProjectIteration.setProject(hProject);

        HDocument hDocument =
                new HDocument(docId, "message.po", "/src/main/resources",
                        ContentType.PO, sourceLocale);
        hDocument.setProjectIteration(hProjectIteration);
        HTextFlow hTextFlow =
                new HTextFlow(hDocument, "resId" + id);
        hTextFlow.setContents("hello world " + id);
        hTextFlow.setId(id);
        hTextFlow.setPos((int) id);

        HTextFlowTarget target = new HTextFlowTarget(hTextFlow, targetLocale);
        target.setVersionNum(0);
        target.setState(contentState);
        target.setLastChanged(new Date());

        hTextFlow.getTargets().put(targetLocale.getId(), target);
        return hTextFlow;
    }

    public static HTextFlow makeApprovedHTextFlow(long id, HLocale targetLocale) {
        return makeHTextFlow(id, new HLocale(LocaleId.EN_US), targetLocale, ContentState.Approved,
                "pot/message.pot", "versionSlug", "projectSlug");
    }

    private static HTextFlow makeTextFlowInProject(long textFlowId, HLocale sourceLocale,
            HLocale targetLocale, ContentState contentState, String docId,
            String versionSlug, String projectSlug) {
        HProject hProject = makeProject(projectSlug);
        HProjectIteration hProjectIteration =
                makeProjectIteration(hProject, versionSlug);

        HDocument hDocument =
                makeDocument(hProjectIteration, sourceLocale, docId);
        HTextFlow hTextFlow = makeTextFlow(hDocument, textFlowId);

        HTextFlowTarget target =
                makeTextFlowTarget(hTextFlow, targetLocale, contentState);

        hTextFlow.getTargets().put(targetLocale.getId(), target);
        return hTextFlow;
    }

    @Nonnull
    public static HTextFlowTarget makeTextFlowTarget(HTextFlow hTextFlow,
            HLocale targetLocale,
            ContentState contentState) {
        HTextFlowTarget target = new HTextFlowTarget(hTextFlow, targetLocale);
        target.setVersionNum(0);
        target.setState(contentState);
        target.setLastChanged(new Date());
        return target;
    }

    @Nonnull
    private static HDocument makeDocument(HProjectIteration hProjectIteration,
            HLocale sourceLocale, String docId) {
        HDocument hDocument =
                new HDocument(docId, "message.po", "src/main/resources",
                        ContentType.PO, sourceLocale);
        hDocument.setProjectIteration(hProjectIteration);
        return hDocument;
    }

    @Nonnull
    private static HProjectIteration makeProjectIteration(HProject hProject,
            String versionSlug) {
        HProjectIteration hProjectIteration = new HProjectIteration();
        hProjectIteration.setSlug(versionSlug);
        hProjectIteration.setProject(hProject);
        return hProjectIteration;
    }

    @Nonnull
    private static HProject makeProject(String projectSlug) {
        HProject hProject = new HProject();
        hProject.setSlug(projectSlug);
        return hProject;
    }

    public static TransMemoryUnit makeTransMemoryUnit(Long l, HLocale hLocale) {
        TransMemory tm = new TransMemory();
        tm.setSlug("test-tm");

        return TransMemoryUnit.tu(tm, "uid" + l, "uid" + l, hLocale
                        .getLocaleId().getId(), "<seg>source</seg>",
                TransMemoryUnitVariant.tuv("lang", "<seg>target</seg>"));
    }

    public static void setId(ModelEntityBase entity, Long id) {
        try {
            // alternative(commons-lang3) FieldUtils.writeField(entity, "setId", id, true);
            Method setIdMethod = ModelEntityBase.class
                    .getDeclaredMethod("setId", Long.class);
            setIdMethod.setAccessible(true);
            setIdMethod.invoke(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
