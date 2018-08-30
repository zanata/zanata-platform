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
package org.zanata.feature.clientserver

import com.google.common.io.Files
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.xml.sax.InputSource
import org.zanata.adapter.po.PoReader2
import org.zanata.common.LocaleId
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.webtrans.EditorPage
import org.zanata.page.webtrans.Plurals.SourceSingular
import org.zanata.page.webtrans.Plurals.SourcePlural
import org.zanata.page.webtrans.Plurals.TargetSingular
import org.zanata.page.webtrans.Plurals.TargetPluralOne
import org.zanata.page.webtrans.Plurals.TargetPluralTwo
import org.zanata.rest.dto.resource.TextFlow
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.ClientWorkFlow
import org.zanata.workflow.LoginWorkFlow

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.MavenHome.mvn
import org.zanata.feature.clientserver.ProjectMaintainerTest.Companion.MAVEN_PLUGIN


/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@Category(DetailedTest::class)
class GettextPluralSupportTest : ZanataTestCase() {

    private val client = ClientWorkFlow()
    private lateinit var restCaller: ZanataRestCaller

    private val tempDir = Files.createTempDir()

    private val userConfigPath = ClientWorkFlow.getUserConfigPath("admin")
    private lateinit var projectRootPath: File

    @Before
    @Throws(IOException::class)
    fun setUp() {
        projectRootPath = client.getProjectRootPath("plural")
        Files.copy(File(projectRootPath, "pom.xml"), File(tempDir,
                "pom.xml"))
        Files.copy(File(projectRootPath, "zanata.xml"), File(tempDir,
                "zanata.xml"))
        val potDir = File(tempDir, "pot")
        potDir.mkdirs()
        val plDir = File(tempDir, "pl")
        plDir.mkdirs()
        Files.copy(File(projectRootPath.toString() + "/pot", "test.pot"),
                File(potDir, "test.pot"))
        Files.copy(File(projectRootPath.toString() + "/pl", "test.po"),
                File(plDir, "test.po"))
        restCaller = ZanataRestCaller()
    }

    @Trace(summary = "The user can push and pull gettext plural projects")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    @Throws(IOException::class)
    fun canPushAndPullPlural() {
        restCaller.createProjectAndVersion("plurals", "master", "podir")
        var output = client.callWithTimeout(tempDir, "${mvn()} -e -B " +
                "$MAVEN_PLUGIN:push -Dzanata.pushType=both " +
                "-Dzanata.userConfig=$userConfigPath")

        assertThat(client.isPushSuccessful(output)).isTrue()

        val editorPage = verifyPluralPushedToEditor()

        val pullDir = Files.createTempDir()
        val pullDirPath = pullDir.absolutePath
        val command = ("${mvn()} -e -B $MAVEN_PLUGIN:pull " +
                "-Dzanata.pullType=both " +
                "-Dzanata.srcDir=$pullDirPath " +
                "-Dzanata.transDir=$pullDirPath " +
                "-Dzanata.userConfig=$userConfigPath")
        output = client.callWithTimeout(tempDir, command)
        assertThat(client.isPushSuccessful(output)).isTrue()

        // source round trip
        val originalTextFlows = getTextFlows(File(projectRootPath.toString() +
                "/pot/test.pot"))
        val pulledTextFlows = getTextFlows(File(pullDir, "test.pot"))
        assertThat(pulledTextFlows).isEqualTo(originalTextFlows)

        // translation round trip
        val originalTargets = getTextFlowTargets(
                File(projectRootPath.toString() + "/pl/test.po"))
        val pulledTargets = getTextFlowTargets(
                File(pullDir.toString() + "/pl/test.po"))
        assertThat(pulledTargets).isEqualTo(originalTargets)

        // translate on web UI and pull again
        editorPage.translateTargetAtRowIndex(0, "one aoeuaouaou")
                .saveAsFuzzyAtRow(0)


        client.callWithTimeout(tempDir, command)
        val newContents = getTextFlowTargets(
                File(pullDir.toString() + "/pl/test.po"))[0].contents
        assertThat(newContents).contains("one aoeuaouaou")

    }

    private fun verifyPluralPushedToEditor(): EditorPage {
        // verify first message
        LoginWorkFlow().signIn("admin", "admin")
        val editorPage = BasicWorkFlow()
                .goToEditor("plurals", "master", "pl", "test")

        assertThat(editorPage.getMessageSourceAtRowIndex(0, SourceSingular))
                .isEqualTo("One file removed")
        assertThat(editorPage.getMessageSourceAtRowIndex(0, SourcePlural))
                .isEqualTo("%d files removed")
        // nplural for Polish is 3

        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0,
                TargetSingular))
                .isEqualTo("1 aoeuaouaou")
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0,
                TargetPluralOne))
                .isEqualTo("%d aoeuaouao")
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0,
                TargetPluralTwo))
                .isEqualTo("")

        return editorPage
    }

    @Throws(FileNotFoundException::class)
    private fun getTextFlows(file: File): List<TextFlow> {
        return PoReader2().extractTemplate(
                InputSource(FileInputStream(file)), LocaleId.EN_US,
                file.name).textFlows
    }

    @Throws(FileNotFoundException::class)
    private fun getTextFlowTargets(file: File): List<TextFlowTarget> {
        return PoReader2().extractTarget(
                InputSource(FileInputStream(file)))
                .textFlowTargets
    }
}
