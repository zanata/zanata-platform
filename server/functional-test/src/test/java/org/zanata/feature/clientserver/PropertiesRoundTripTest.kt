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
import org.fedorahosted.openprops.Properties
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.webtrans.EditorPage
import org.zanata.util.TestFileGenerator
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.ClientWorkFlow
import org.zanata.workflow.LoginWorkFlow

import java.io.File
import java.io.FileReader
import java.io.FileWriter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.zanata.util.mvn
import org.zanata.feature.clientserver.ProjectMaintainerTest.Companion.MAVEN_PLUGIN

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class PropertiesRoundTripTest : ZanataTestCase() {


    private val client = ClientWorkFlow()
    private val tempDir = Files.createTempDir()

    private val userConfigPath = ClientWorkFlow.getUserConfigPath("admin")

    @BeforeEach
    fun setUp() {
        // generate a properties source
        val properties = Properties().apply {
            setProperty("hello", "hello world")
            setProperty("greeting", "this is from Houston")
            setProperty("hey", "hey hey")
        }
        val propertiesSource = File(tempDir, "test.properties")
        properties.store(FileWriter(propertiesSource), "comment")
    }

    @Trace(summary = "The maintainer user may push and pull properties files")
    @Test
    fun canPushAndPullProperties() {
        zanataRestCaller.createProjectAndVersion("properties-test", "master",
                "properties")
        // generate a zanata.xml
        TestFileGenerator.generateZanataXml(File(tempDir, "zanata.xml"),
                "properties-test", "master", "properties", arrayListOf("pl"))
        var output = client.callWithTimeout(tempDir, "$mvn -B " +
                "$MAVEN_PLUGIN:push " +
                "-Dzanata.srcDir=. " +
                "-Dzanata.userConfig=$userConfigPath")

        assertThat(client.isPushSuccessful(output)).isTrue()

        var editorPage = verifyPushedToEditor()
        editorPage = editorPage.translateTargetAtRowIndex(2,
                "translation updated approved")
                .approveTranslationAtRow(2)

        editorPage.translateTargetAtRowIndex(1, "translation updated fuzzy")
                .saveAsFuzzyAtRow(1)

        output = client.callWithTimeout(tempDir, "$mvn -e -B " +
                "$MAVEN_PLUGIN:pull " +
                "-Dzanata.userConfig=$userConfigPath")

        assertThat(client.isPushSuccessful(output)).isTrue()
        val transFile = File(tempDir, "test_pl.properties")
        assertThat(transFile.exists()).isTrue()
        val translations = Properties()
        translations.load(FileReader(transFile))
        assertThat(translations.size()).isEqualTo(1)
        assertThat(translations.getProperty("hey"))
                .isEqualTo("translation updated approved")

        // change on client side
        translations.setProperty("greeting", "translation updated on client")
        translations.store(FileWriter(transFile), null)

        // push again
        client.callWithTimeout(tempDir,
                "$mvn -e -B $MAVEN_PLUGIN:push " +
                        "-Dzanata.pushType=trans -Dzanata.srcDir=. " +
                        "-Dzanata.userConfig=$userConfigPath")

        val editor = BasicWorkFlow().goToEditor("properties-test",
                "master", "pl", "test")
        assertThat(editor.getBasicTranslationTargetAtRowIndex(1))
                .isEqualTo("translation updated on client")
    }

    private fun verifyPushedToEditor(): EditorPage {
        LoginWorkFlow().signIn("admin", "admin")
        val editorPage = BasicWorkFlow().goToEditor("properties-test",
                "master", "pl", "test")

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("hello world")
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("this is from Houston")
        assertThat(editorPage.getMessageSourceAtRowIndex(2))
                .isEqualTo("hey hey")

        return editorPage
    }
}
