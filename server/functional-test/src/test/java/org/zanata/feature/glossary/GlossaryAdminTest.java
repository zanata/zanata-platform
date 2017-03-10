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
package org.zanata.feature.glossary;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.workflow.ClientWorkFlow;
import java.io.File;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.MavenHome.mvn;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Category(DetailedTest.class)
public class GlossaryAdminTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlossaryAdminTest.class);

    private ClientWorkFlow clientWorkFlow = new ClientWorkFlow();

    /**
     * Validates that a pushed glossary appears in the Glossary table. After
     * pushing, a table with Glossary statistics should be shown. Validate that
     * the number of glossary entries per language matches the number of entries
     * pushed from each of the test cases mentioned in the Setup.
     */
    @Feature(summary = "A user can push glossaries to Zanata",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181711)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void testGlossaryView() {
        // Push a glossary
        File projectRootPath = clientWorkFlow.getProjectRootPath("glossary");
        String userConfigPath =
                ClientWorkFlow.getUserConfigPath("glossaryadmin");
        List<String> result = clientWorkFlow.callWithTimeout(projectRootPath,
                mvn() + " -e -U --batch-mode zanata:glossary-push -Dglossary.lang=hi -Dzanata.file=compendium.csv -Dzanata.userConfig="
                        + userConfigPath);
        assertThat(clientWorkFlow.isPushSuccessful(result), Matchers.is(true));
    }
}
