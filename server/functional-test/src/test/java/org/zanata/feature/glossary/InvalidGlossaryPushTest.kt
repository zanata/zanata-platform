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
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.ClientWorkFlow
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.MavenHome.mvn

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class InvalidGlossaryPushTest : ZanataTestCase() {

    private val pushCommand = "${mvn()} -e --batch-mode zanata:glossary-push -Dglossary.lang=fr -Dzanata.file=compendium_fr_invalid.po -Dzanata.userConfig="
    private lateinit var clientWorkFlow: ClientWorkFlow
    private lateinit var userConfigPath: String
    private lateinit var projectRootPath: File

    @BeforeEach
    fun before() {
        clientWorkFlow = ClientWorkFlow()
        projectRootPath = clientWorkFlow.getProjectRootPath("glossary")
        userConfigPath = ClientWorkFlow.getUserConfigPath("glossarist")
    }

    @Test
    @Trace(summary = "Invalid glossary file will be rejected by the server")
    fun invalidGlossaryPush() {
        val result = push(pushCommand, userConfigPath)
        val output = resultByLines(result)
        log.info("output:\n{}", output)
        assertThat(output).containsIgnoringCase("unexpected token")
        assertThat(clientWorkFlow.isPushSuccessful(result))
                .`as`("glossary push should fail").isFalse()
    }

    fun push(command: String, configPath: String?): List<String> {
        return clientWorkFlow.callWithTimeout(projectRootPath,
                command + configPath!!)
    }

    fun resultByLines(output: List<String>): String {
        return Joiner.on("\n").join(output)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(InvalidGlossaryPushTest::class.java)
    }
}
