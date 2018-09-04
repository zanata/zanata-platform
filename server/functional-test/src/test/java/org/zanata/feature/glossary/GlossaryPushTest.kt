/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.glossary

import com.google.common.base.Joiner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.ClientWorkFlow
import org.zanata.workflow.LoginWorkFlow
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.mvn

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class GlossaryPushTest : ZanataTestCase() {

    private lateinit var clientWorkFlow: ClientWorkFlow
    private lateinit var projectRootPath: File
    private lateinit var userConfigPath: String
    private val pushCommand = "$mvn -e -U --batch-mode zanata:glossary-push -Dglossary.lang=fr -Dzanata.file=compendium_fr.po -Dzanata.userConfig="
    private val pushCSVCommand = "$mvn -e -U --batch-mode zanata:glossary-push -Dzanata.file=compendium_invalid.csv -Dglossary.lang=hi -Dzanata.userConfig="

    @BeforeEach
    fun before() {
        clientWorkFlow = ClientWorkFlow()
        projectRootPath = clientWorkFlow.getProjectRootPath("glossary")
        userConfigPath = ClientWorkFlow.getUserConfigPath("glossarist")
    }

    @Test
    fun successfulGlossaryPush() {
        val result = push(pushCommand, userConfigPath)
        log.info(resultByLines(result))
        assertThat(clientWorkFlow.isPushSuccessful(result))
                .describedAs("glossary push should succeed").isTrue()
        val editorPage = LoginWorkFlow().signIn("admin", "admin")
                .gotoExplore().searchAndGotoProjectByName("about fedora")
                .gotoVersion("master").translate("fr", "About_Fedora")
        editorPage.searchGlossary("filesystem")
        assertThat(editorPage.glossaryResultTable[1][2])
                .describedAs("first glossary result")
                .isEqualTo("système de fichiers")
    }

    @Test
    fun failedCSVGlossaryPush() {
        val resultList = push(pushCSVCommand, userConfigPath)
        val output = resultByLines(resultList)
        log.info("output:\n{}", output)
        assertThat(output).containsIgnoringCase("Invalid CSV file")
        assertThat(clientWorkFlow.isPushSuccessful(resultList))
                .describedAs("glossary push should not succeed").isFalse()
    }

    private fun push(command: String, configPath: String?): List<String> {
        return clientWorkFlow.callWithTimeout(projectRootPath,
                command + configPath)
    }

    private fun resultByLines(output: List<String>): String {
        return Joiner.on("\n").join(output)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(GlossaryPushTest::class.java)
    }
    // this is just a subset of successfulGlossaryPush, and doesn't seem
    // to test authorisation at all.
    // @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    // public void unauthorizedGlossaryPushRejected() throws Exception {
    // List<String> result = clientWorkFlow .callWithTimeout(
    // projectRootPath, pushCommand + userConfigPath);
    // assertThat(clientWorkFlow.isPushSuccessful(result))
    // .as("glossary push should succeed")
    // .isTrue();
    // }
}
