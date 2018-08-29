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
package org.zanata.feature.rest

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.zanata.common.LocaleId
import org.zanata.util.ZanataRestCaller

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class CopyTransTest {

    private val copyTrans = true

    @Test
    @Disabled("see https://github.com/zanata/zanata-server/pull/571#issuecomment-55547577 " +
            "this test can be used to reproduce that issue." +
            "See org.zanata.rest.service.ResourceUtils.transferFromTextFlows()")
    fun testPushTranslationAndCopyTrans() {
        val restCaller = ZanataRestCaller()
        val projectSlug = "push-test"
        val iterationSlug = "master"
        val projectType = "gettext"
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType)

        val docId = "messages"
        val sourceResource = ZanataRestCaller.buildSourceResource(docId)
        val transResource = ZanataRestCaller.buildTranslationResource()
        val numOfMessages = 520
        for (i in 0 until numOfMessages) {
            val resId = "res$i"
            val content = "content$i"
            sourceResource.textFlows.add(ZanataRestCaller.buildTextFlow(resId, content))
            transResource.textFlowTargets.add(
                    ZanataRestCaller.buildTextFlowTarget(resId, content))
        }
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource, false)
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId,
                LocaleId("pl"), transResource, "import", false)

        // create another version
        restCaller.createProjectAndVersion(projectSlug, "2", projectType)
        restCaller.asyncPushSource(projectSlug, "2", sourceResource, false)
        restCaller.asyncPushTarget(projectSlug, "2", docId, LocaleId("pl"),
                transResource, "import", false)

        // push to old version again
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource,
                copyTrans)
    }

    @Test
    @Disabled("see https://github.com/zanata/zanata-server/pull/571#issuecomment-56011217 " +
            "this test can be used to reproduce that issue." +
            "See org.zanata.dao.TextFlowDAO.getByDocumentAndResIds")
    fun testPushTranslationRepeatedly() {
        val restCaller = ZanataRestCaller()
        val projectSlug = "push-test"
        val iterationSlug = "master"
        val projectType = "gettext"
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType)

        val docId = "messages"
        val sourceResource = ZanataRestCaller.buildSourceResource(docId)
        val transResource = ZanataRestCaller.buildTranslationResource()
        val numOfMessages = 10
        for (i in 0 until numOfMessages) {
            val resId = "res$i"
            val content = "content$i"
            sourceResource.textFlows.add(ZanataRestCaller.buildTextFlow(resId, content))
            transResource.textFlowTargets.add(
                    ZanataRestCaller.buildTextFlowTarget(resId, content))
        }
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource,
                false)
        val localeId = LocaleId("pl")
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId,
                localeId, transResource, "auto", false)
        restCaller.runCopyTrans(projectSlug, iterationSlug, docId)

        // create some obsolete text flows
        val updatedSource = ZanataRestCaller.buildSourceResource(docId)
        val updatedTransResource = ZanataRestCaller.buildTranslationResource()
        for (i in 0 until numOfMessages) {
            val resId = "res$i"
            val content = "content$i changed"
            updatedSource.textFlows.add(ZanataRestCaller.buildTextFlow(resId, content))
            updatedTransResource.textFlowTargets.add(ZanataRestCaller.buildTextFlowTarget(resId, content))
        }

        // push updated source (same resId different content)
        restCaller.asyncPushSource(projectSlug, iterationSlug, updatedSource, false)
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId, localeId,
                updatedTransResource, "auto", false)

        // push again
        restCaller.asyncPushSource(projectSlug, iterationSlug, updatedSource, false)
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId, localeId,
                updatedTransResource, "auto", false)
    }
}
