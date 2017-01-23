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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import java.io.File;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.MavenHome.mvn;

/**
 * @see <a href="https://tcms.engineering.redhat.com/case/147311/">TCMS case</a>
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class GlossaryPushTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlossaryPushTest.class);

    private ClientWorkFlow clientWorkFlow;
    private File projectRootPath;
    private String userConfigPath;
    private String pushCommand = mvn()
            + " -e -U --batch-mode zanata:glossary-push -Dglossary.lang=fr -Dzanata.file=compendium_fr.po -Dzanata.userConfig=";
    private String pushCSVCommand = mvn()
            + " -e -U --batch-mode zanata:glossary-push -Dzanata.file=compendium_invalid.csv -Dglossary.lang=hi -Dzanata.userConfig=";

    @Before
    public void before() {
        clientWorkFlow = new ClientWorkFlow();
        projectRootPath = clientWorkFlow.getProjectRootPath("glossary");
        userConfigPath = ClientWorkFlow.getUserConfigPath("glossarist");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void successfulGlossaryPush() throws Exception {
        List<String> result = push(pushCommand, userConfigPath);
        log.info(resultByLines(result));
        assertThat(clientWorkFlow.isPushSuccessful(result))
                .as("glossary push should succeed").isTrue();
        EditorPage editorPage = new LoginWorkFlow().signIn("admin", "admin")
                .gotoExplore().searchAndGotoProjectByName("about fedora")
                .gotoVersion("master").translate("fr", "About_Fedora");
        editorPage.searchGlossary("filesystem");
        assertThat(editorPage.getGlossaryResultTable().get(1).get(2))
                .as("first glossary result").isEqualTo("système de fichiers");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void failedCSVGlossaryPush() throws Exception {
        List<String> resultList = push(pushCSVCommand, userConfigPath);
        String output = resultByLines(resultList);
        log.info("output:\n{}", output);
        assertThat(output).containsIgnoringCase("Invalid CSV file");
        assertThat(clientWorkFlow.isPushSuccessful(resultList))
                .as("glossary push should not succeed").isFalse();
    }

    private List<String> push(String command, String configPath)
            throws Exception {
        return clientWorkFlow.callWithTimeout(projectRootPath,
                command + configPath);
    }

    private String resultByLines(List<String> output) {
        return Joiner.on("\n").join(output);
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
