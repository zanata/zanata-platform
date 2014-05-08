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

import lombok.extern.slf4j.Slf4j;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.glossary.GlossaryPage;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Category(DetailedTest.class)
@Slf4j
public class GlossaryAdminTest extends ZanataTestCase {

    @Rule
    public TestRule sampleProjectRule = new SampleProjectRule();

    private ClientWorkFlow clientWorkFlow = new ClientWorkFlow();

    /**
     * Validates that a pushed glossary appears in the Glossary table.
     * After pushing, a table with Glossary statistics should be shown.
     * Validate that the the number of glossary entries per language matches
     * the number of entries pushed from each of the test cases metnioned in the
     * Setup.
     * @see TCMS Test Case 181711
     */
    @Test
    public void testGlossaryView() {
        // Push a glossary
        File projectRootPath =
                clientWorkFlow.getProjectRootPath("glossary");
        String userConfigPath =
                ClientWorkFlow.getUserConfigPath("glossaryadmin");

        List<String> result =
                clientWorkFlow
                        .callWithTimeout(
                                projectRootPath,
                                "mvn --batch-mode zanata:glossary-push -Dglossary.lang=hi -Dzanata.glossaryFile=compendium.csv -Dzanata.userConfig="
                                        + userConfigPath);

        assertThat(clientWorkFlow.isPushSuccessful(result),
                Matchers.is(true));

        // Make sure glossary shows up on the page
        GlossaryPage glossaryPage =
                new LoginWorkFlow().signIn("admin", "admin").goToGlossary();
        List<String> langs = glossaryPage.getAvailableGlossaryLanguages();

        assertThat(langs.size(), greaterThan(0));
        assertThat(langs, containsInAnyOrder("pl", "hi", "en-US"));
        assertThat(glossaryPage.getGlossaryEntryCount("pl"), greaterThan(1));
        assertThat(glossaryPage.getGlossaryEntryCount("hi"), greaterThan(1));
        assertThat(glossaryPage.getGlossaryEntryCount("en-US"), greaterThan(1));
    }
}
