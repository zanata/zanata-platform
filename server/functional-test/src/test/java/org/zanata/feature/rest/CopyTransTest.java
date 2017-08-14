/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.rest;

import org.junit.Ignore;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.ZanataRestCaller;

import static org.zanata.util.ZanataRestCaller.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CopyTransTest {

    private boolean COPYTRANS = true;

    @Test
    @Ignore("see https://github.com/zanata/zanata-server/pull/571#issuecomment-55547577 this test can be used to reproduce that issue.")
    // see org.zanata.rest.service.ResourceUtils.transferFromTextFlows()
    public void testPushTranslationAndCopyTrans() {
        ZanataRestCaller restCaller =
                new ZanataRestCaller();
        String projectSlug = "push-test";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);

        String docId = "messages";
        Resource sourceResource = buildSourceResource(docId);
        TranslationsResource transResource = buildTranslationResource();
        int numOfMessages = 520;
        for (int i = 0; i < numOfMessages; i++) {
            String resId = "res" + i;
            String content = "content" + i;
            sourceResource.getTextFlows().add(buildTextFlow(resId, content));
            transResource.getTextFlowTargets().add(
                    buildTextFlowTarget(resId, content));
        }
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource, false);
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId,
                new LocaleId("pl"), transResource, "import", false);

        // create another version
        restCaller.createProjectAndVersion(projectSlug, "2", projectType);
        restCaller.asyncPushSource(projectSlug, "2", sourceResource, false);
        restCaller.asyncPushTarget(projectSlug, "2", docId, new LocaleId("pl"),
                transResource, "import", false);

        // push to old version again
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource,
                COPYTRANS);
    }

    @Test
    @Ignore("see https://github.com/zanata/zanata-server/pull/571#issuecomment-56011217 this test can be used to reproduce that issue.")
    // see org.zanata.dao.TextFlowDAO.getByDocumentAndResIds
    public
            void testPushTranslationRepeatedly() {
        ZanataRestCaller restCaller =
                new ZanataRestCaller();
        String projectSlug = "push-test";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);

        String docId = "messages";
        Resource sourceResource = buildSourceResource(docId);
        TranslationsResource transResource = buildTranslationResource();
        int numOfMessages = 10;
        for (int i = 0; i < numOfMessages; i++) {
            String resId = "res" + i;
            String content = "content" + i;
            sourceResource.getTextFlows().add(buildTextFlow(resId, content));
            transResource.getTextFlowTargets().add(
                    buildTextFlowTarget(resId, content));
        }
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource,
                false);
        LocaleId localeId = new LocaleId("pl");
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId,
                localeId, transResource, "auto", false);
        restCaller.runCopyTrans(projectSlug, iterationSlug, docId);

        // create some obsolete text flows
        Resource updatedSource = buildSourceResource(docId);
        TranslationsResource updatedTransResource = buildTranslationResource();
        for (int i = 0; i < numOfMessages; i++) {
            String resId = "res" + i;
            String content = "content" + i + " changed";
            updatedSource.getTextFlows().add(buildTextFlow(resId, content));
            updatedTransResource.getTextFlowTargets().add(buildTextFlowTarget(resId, content));
        }

        // push updated source (same resId different content)
        restCaller.asyncPushSource(projectSlug, iterationSlug, updatedSource, false);
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId, localeId, updatedTransResource, "auto", false);

        // push again
        restCaller.asyncPushSource(projectSlug, iterationSlug, updatedSource, false);
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId, localeId, updatedTransResource, "auto", false);
    }
}
