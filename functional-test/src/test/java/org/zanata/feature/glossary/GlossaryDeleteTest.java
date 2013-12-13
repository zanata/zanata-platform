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

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.zanata.feature.ConcordionTest;
import org.zanata.page.administration.ManageSearchPage;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.RetryRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.TakeScreenshotRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientPushWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @see <a href="https://tcms.engineering.redhat.com/case/167899/">TCMS case</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(ConcordionTest.class)
@Slf4j
public class GlossaryDeleteTest {
    @Rule
    public TestRule sampleProjectRule = new SampleProjectRule();

    @Rule
    public TakeScreenshotRule screenshotRule = new TakeScreenshotRule();

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    private ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();

    @Test
    public void testGlossaryDelete() {
        File projectRootPath =
                clientPushWorkFlow.getProjectRootPath("glossary");
        String userConfigPath =
                ClientPushWorkFlow.getUserConfigPath("glossaryadmin");

        List<String> result =
                clientPushWorkFlow
                        .callWithTimeout(
                            projectRootPath,
                            "mvn --batch-mode zanata:glossary-push -Dglossary.lang=hi -Dzanata.glossaryFile=compendium.csv -Dzanata.userConfig="
                                + userConfigPath);

        assertThat(clientPushWorkFlow.isPushSuccessful(result),
                Matchers.is(true));

        result =
                clientPushWorkFlow.callWithTimeout(projectRootPath,
                    "mvn --batch-mode zanata:glossary-delete -Dzanata.lang=hi -Dzanata.userConfig="
                        + userConfigPath);

        assertThat(clientPushWorkFlow.isPushSuccessful(result),
                Matchers.is(true));

        new LoginWorkFlow().signIn("admin", "admin");
        // for some reason on jenkins sometimes the index is out of sync.
        ManageSearchPage manageSearchPage =
                new BasicWorkFlow().goToPage("admin/search", ManageSearchPage.class);
        manageSearchPage.selectAllActionsFor("HGlossaryEntry");
        manageSearchPage.selectAllActionsFor("HGlossaryTerm");
        manageSearchPage.performSelectedActions();

        List<List<String>> hiGlossaryResult =
                translate("hi").searchGlossary("hello")
                        .getGlossaryResultTable();
        assertThat(hiGlossaryResult, Matchers.empty());

        List<List<String>> plGlossaryResult =
                translate("pl").searchGlossary("hello")
                        .getGlossaryResultTable();
        // 2 row 2 column is glossary target
        assertThat(plGlossaryResult.get(1).get(1), Matchers.equalTo("cześć"));
    }

    public EditorPage translate(String locale) {
        return new BasicWorkFlow().goToPage(
                "webtrans/translate?project=about-fedora&iteration=master&localeId="
                        + locale + "&locale=en#view:doc;doc:About_Fedora",
                EditorPage.class);
    }
}
