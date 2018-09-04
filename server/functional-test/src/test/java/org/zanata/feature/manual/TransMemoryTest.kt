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

package org.zanata.feature.manual

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.LanguageWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
// This test class only sets up test data on server.
// It's not verifying any TM features.
@DisplayName("TransMemoryTest")
@Tag("Manual")
class TransMemoryTest : ZanataTestCase() {

    @Test
    fun `Push a Translation Memory to a Project`() {
        LoginWorkFlow().signIn("admin", "admin")
        val languageWorkFlow = LanguageWorkFlow()
        languageWorkFlow.addLanguageAndJoin("en-US")
        languageWorkFlow.addLanguageAndJoin("zh-Hans")

        val projectWorkFlow = ProjectWorkFlow()
        projectWorkFlow.createNewSimpleProject("trans-memory",
                "trans memory test")
        projectWorkFlow.createNewProjectVersion("trans memory test", "master")

        // int exitCode = new ClientWorkFlow().mvnPush("trans-memory",
        // "-Dzanata.merge=import", "-Dzanata.projectVersion=master",
        // "-Dzanata.pushType=Both");

        // assertThat(exitCode, Matchers.equalTo(0));
        assert(true)
    }

    @Test
    fun pushTransMemoryProjectWithDifferentProjectName() {
        LoginWorkFlow().signIn("admin", "admin")
        val languageWorkFlow = LanguageWorkFlow()
        languageWorkFlow.addLanguageAndJoin("en-US")
        languageWorkFlow.addLanguageAndJoin("zh-Hans")

        val projectWorkFlow = ProjectWorkFlow()
        projectWorkFlow.createNewSimpleProject("trans-memory-v2",
                "trans memory test v2")
        projectWorkFlow.createNewProjectVersion("trans memory test v2",
                "master")

        // @formatter:off
        //      int exitCode = new ClientWorkFlow().mvnPush("trans-memory",
        //            "-Dzanata.projectConfig=differentProject/zanata.xml",
        //            "-Dzanata.projectVersion=master",
        //            "-Dzanata.copyTrans=false",
        //            "-Dzanata.pushType=Source");
        // @formatter:on

        // assertThat(exitCode, Matchers.equalTo(0));
        assert(true)
    }
}
