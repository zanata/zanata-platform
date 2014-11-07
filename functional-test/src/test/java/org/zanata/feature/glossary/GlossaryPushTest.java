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
package org.zanata.feature.glossary;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see <a href="https://tcms.engineering.redhat.com/case/147311/">TCMS case</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@Category(DetailedTest.class)
public class GlossaryPushTest extends ZanataTestCase {

    @Rule
    public TestRule sampleProjectRule = new SampleProjectRule();

    private ClientWorkFlow clientWorkFlow;
    private File projectRootPath;
    private String userConfigPath;
    private String basicUserConfigPath;

    private String pushCommand = "mvn --batch-mode zanata:glossary-push "+
            "-Dglossary.lang=fr -Dzanata.glossaryFile=compendium_fr.po "+
            "-Dzanata.userConfig=";

    private String pushCSVCommand = "mvn --batch-mode zanata:glossary-push "+
            "-Dzanata.glossaryFile=compendium_invalid.csv -Dglossary.lang=hi "+
            "-Dzanata.userConfig=";

    @Before
    public void before() {
        clientWorkFlow = new ClientWorkFlow();
        projectRootPath = clientWorkFlow.getProjectRootPath("glossary");
        userConfigPath = ClientWorkFlow.getUserConfigPath("glossarist");
        basicUserConfigPath = ClientWorkFlow.getUserConfigPath("translator");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void successfulGlossaryPush() throws Exception {
        List<String> result = push(pushCommand, userConfigPath);
        log.info(resultByLines(result));

        assertThat(clientWorkFlow.isPushSuccessful(result))
                .isTrue()
                .as("The glossary push was successful");

        EditorPage editorPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoVersion("master")
                .translate("fr", "About_Fedora");

        editorPage.searchGlossary("filesystem");

        assertThat(editorPage.getGlossaryResultTable().get(1).get(1))
                .isEqualTo("système de fichiers")
                .as("The first glossary result is correct");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void failedCSVGlossaryPush() throws Exception {
        List<String> result = push(pushCSVCommand, userConfigPath);
        log.info(resultByLines(result));

        assertThat(clientWorkFlow.isPushSuccessful(result))
                .isFalse()
                .as("The glossary push was not successful");
    }

    private List<String> push(String command, String configPath)
            throws Exception {
        return clientWorkFlow.callWithTimeout(projectRootPath, command
                + configPath);
    }

    private String resultByLines(List<String> output) {
        return Joiner.on("\n").join(output);
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void unauthorizedGlossaryPushRejected() throws Exception {
        List<String> result = clientWorkFlow .callWithTimeout(
                projectRootPath, pushCommand + userConfigPath);
        assertThat(clientWorkFlow.isPushSuccessful(result)).isTrue()
                .as("Glossary push was successful");
    }

}
