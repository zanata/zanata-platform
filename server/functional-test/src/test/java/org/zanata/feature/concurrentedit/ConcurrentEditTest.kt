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
package org.zanata.feature.concurrentedit

import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.common.LocaleId
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.ZanataRestCaller.buildSourceResource
import org.zanata.util.ZanataRestCaller.buildTextFlow
import org.zanata.util.ZanataRestCaller.buildTextFlowTarget
import org.zanata.util.ZanataRestCaller.buildTranslationResource

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@Category(DetailedTest::class)
class ConcurrentEditTest : ZanataTestCase() {
    private lateinit var restCaller: ZanataRestCaller

    @Before
    fun setUp() {
        restCaller = ZanataRestCaller()
    }

    @Trace(summary = "The system will propagate translations done by upload " +
            "and copyTrans to editor")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun editorReceivesRestServiceResults() {
        // create project and push source
        val projectSlug = "base"
        val iterationSlug = "master"
        val projectType = "gettext"
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType)
        val docId = "test.pot"
        val sourceResource = buildSourceResource(docId,
                buildTextFlow("res1", "hello world"),
                buildTextFlow("res2", "greetings"))
        restCaller.postSourceDocResource(projectSlug, iterationSlug,
                sourceResource, false)
        // open editor
        LoginWorkFlow().signIn("admin", "admin")
        // webTrans
        val editorPage = BasicWorkFlow()
                .goToEditor("base", "master", "pl", "test.pot")
        val translation = editorPage.getMessageTargetAtRowIndex(0)
        // for some reason getText() will return one space in it
        assertThat(translation.trim { it <= ' ' }).isEmpty()
        // push target
        val translationsResource = buildTranslationResource(
                buildTextFlowTarget("res1", "hello world translated"))
        restCaller.postTargetDocResource(projectSlug, iterationSlug, docId,
                LocaleId("pl"), translationsResource, "auto")
        // REST push broadcast event to editor
        assertThat(editorPage.expectBasicTranslationAtRowIndex(0,
                "hello world translated")).isTrue()
    }

    @Trace(summary = "The system will show concurrently changed translations " +
            "to the web editor user")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun editorReceivesCopyTransResults() {
        // create project and populate master version
        val projectSlug = "base"
        val iterationSlug = "master"
        val projectType = "gettext"
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType)
        val docId = "test.pot"
        val sourceResource = buildSourceResource(docId,
                buildTextFlow("res1", "hello world"),
                buildTextFlow("res2", "greetings"))
        restCaller.postSourceDocResource(projectSlug, iterationSlug,
                sourceResource, false)
        val translationsResource = buildTranslationResource(
                buildTextFlowTarget("res1", "hello world translated"))
        restCaller.postTargetDocResource(projectSlug, iterationSlug, docId,
                LocaleId("pl"), translationsResource, "auto")
        // create and push source but disable copyTrans
        restCaller.createProjectAndVersion(projectSlug, "beta", projectType)
        restCaller.postSourceDocResource(projectSlug, "beta", sourceResource,
                false)
        // open editor
        LoginWorkFlow().signIn("admin", "admin")
        // webTrans
        val editorPage = BasicWorkFlow()
                .goToEditor("base", "beta", "pl", "test.pot")
        val translation = editorPage.getMessageTargetAtRowIndex(0)
        // for some reason getText() will return one space in it
        assertThat(translation.trim { it <= ' ' }).isEmpty()
        // run copyTrans
        restCaller.runCopyTrans(projectSlug, "beta", docId)
        // copyTrans broadcast event to editor
        assertThat(editorPage.expectBasicTranslationAtRowIndex(0,
                "hello world translated")).isTrue()
    }
}
