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
package org.zanata.feature.misc;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;
import static org.zanata.workflow.BasicWorkFlow.PROJECT_VERSION_TEMPLATE;


/**
 * TCMS test case <a
 * href="https://tcms.engineering.redhat.com/case/137717/">137717</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ObsoleteTextTest extends ZanataTestCase {

    private ZanataRestCaller restCaller;

    @Before
    public void setUp() throws Exception {
        restCaller = new ZanataRestCaller();
    }

    /**
     * Below is a modified version of what's in TCMS test cases.
     *
     * Project "obsolete-test" is created, and version "master" is created, and
     * source texts (message1 and message2) are pushed. Language fr should be
     * enabled. User "admin" exists and join fr language team.
     *
     * <pre>
     * 1. Open obsolete-test in lang fr.
     * 2. Start editing the first entry and save.
     * 3. Leave the translation editor.
     * 4. In command line: remove message2 but keep message1.
     * 5. In command line: edit message1, remove other entries except the first three.
     * 6. Push with pushTrans=true and copyTrans=true.
     * 7. Back to Web UI: translate and save the 2nd and 3rd entries.
     * 8. Click on "Leave Workspace".
     * 9. Go project page of AboutFedora and Check whether the bem is 100% translated.
     * 10. Enter translation editor for bem and see whether the project and document are 100%translated.
     * </pre>
     */
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void obsoleteTextTest() {
        restCaller
                .createProjectAndVersion("obsolete-test", "master", "gettext");
        Resource resource1 =
                buildSourceResource("message1",
                        buildTextFlow("res1", "message one"),
                        buildTextFlow("res2", "message two"),
                        buildTextFlow("res3", "message three"),
                        buildTextFlow("res4", "message four"));

        Resource resource2 =
                buildSourceResource("message2",
                        buildTextFlow("res1", "message one"),
                        buildTextFlow("res2", "message two"),
                        buildTextFlow("res3", "message three"),
                        buildTextFlow("res4", "message four"));

        restCaller.postSourceDocResource("obsolete-test", "master", resource1,
                false);
        restCaller.postSourceDocResource("obsolete-test", "master", resource2,
                false);

        // edit first entry and save
        new LoginWorkFlow().signIn("admin", "admin");
        final EditorPage editorPage = openEditor();
        editorPage.translateTargetAtRowIndex(0, "message one translated")
                .approveTranslationAtRow(0);

        // delete resource 2
        resource2.getTextFlows().clear();
        restCaller.deleteSourceDoc("obsolete-test", "master", "message2");
        // remove last entry from resource 1
        resource1.getTextFlows().remove(3);
        restCaller.putSourceDocResource("obsolete-test", "master", "message1",
                resource1, true);

        final EditorPage editorPageFinal =
                openEditor()
                        .translateTargetAtRowIndex(1, "message two translated")
                        .approveTranslationAtRow(1)
                        .translateTargetAtRowIndex(2, "translated")
                        .approveTranslationAtRow(2);

        editorPageFinal.waitForPageSilence();
        assertThat(editorPageFinal.getStatistics()).contains("100%");

        VersionLanguagesPage versionPage =
                new BasicWorkFlow().goToPage(String.format(
                        PROJECT_VERSION_TEMPLATE, "obsolete-test", "master"),
                        VersionLanguagesPage.class);
        assertThat(versionPage.getStatisticsForLocale("fr")).isEqualTo("100.0%");
    }

    private static EditorPage openEditor() {
        return new BasicWorkFlow().goToEditor("obsolete-test", "master", "fr",
                "message1");
    }
}
