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
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.workflow.ClientWorkFlow;
import java.io.File;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.MavenHome.mvn;

/**
 * @see <a href="https://tcms.engineering.redhat.com/case/169230/">TCMS case</a>
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class InvalidGlossaryPushTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(InvalidGlossaryPushTest.class);

    private String pushCommand = mvn()
            + " -e --batch-mode zanata:glossary-push -Dglossary.lang=fr -Dzanata.file=compendium_fr_invalid.po -Dzanata.userConfig=";
    private ClientWorkFlow clientWorkFlow;
    private String userConfigPath;
    private File projectRootPath;

    @Before
    public void before() {
        clientWorkFlow = new ClientWorkFlow();
        projectRootPath = clientWorkFlow.getProjectRootPath("glossary");
        userConfigPath = ClientWorkFlow.getUserConfigPath("glossarist");
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Feature(summary = "Invalid glossary file will be rejected by the server")
    public void invalidGlossaryPush() throws Exception {
        List<String> result = push(pushCommand, userConfigPath);
        String output = resultByLines(result);
        log.info("output:\n{}", output);
        assertThat(output).containsIgnoringCase("unexpected token");
        assertThat(clientWorkFlow.isPushSuccessful(result))
                .as("glossary push should fail").isFalse();
    }

    public List<String> push(String command, String configPath)
            throws Exception {
        return clientWorkFlow.callWithTimeout(projectRootPath,
                command + configPath);
    }

    public String resultByLines(List<String> output) {
        return Joiner.on("\n").join(output);
    }
}
