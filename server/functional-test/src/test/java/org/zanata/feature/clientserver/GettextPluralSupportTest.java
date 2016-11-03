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
package org.zanata.feature.clientserver;

import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.common.LocaleId;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.page.webtrans.Plurals;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.MavenHome.mvn;

/**
 * This covers TCMS case <a
 * href="https://tcms.engineering.redhat.com/case/217601/">217601</a> and case
 * <a href="https://tcms.engineering.redhat.com/case/217905/">217905</a>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class GettextPluralSupportTest extends ZanataTestCase {

    private ClientWorkFlow client = new ClientWorkFlow();
    private ZanataRestCaller restCaller;

    private File tempDir = Files.createTempDir();

    private String userConfigPath = ClientWorkFlow
            .getUserConfigPath("admin");
    private File projectRootPath;

    @Before
    public void setUp() throws IOException {
        projectRootPath = client.getProjectRootPath("plural");
        Files.copy(new File(projectRootPath, "pom.xml"), new File(tempDir,
                "pom.xml"));
        Files.copy(new File(projectRootPath, "zanata.xml"), new File(tempDir,
                "zanata.xml"));
        File potDir = new File(tempDir, "pot");
        potDir.mkdirs();
        File plDir = new File(tempDir, "pl");
        plDir.mkdirs();
        Files.copy(new File(projectRootPath + "/pot", "test.pot"), new File(
                potDir, "test.pot"));
        Files.copy(new File(projectRootPath + "/pl", "test.po"), new File(
                plDir, "test.po"));
        restCaller = new ZanataRestCaller();
    }

    @Feature(summary = "The user can push and pull gettext plural projects",
            tcmsTestCaseIds = {217601, 217905}, tcmsTestPlanIds = 5316)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canPushAndPullPlural() throws IOException {
        restCaller.createProjectAndVersion("plurals", "master", "podir");
        List<String> output =
                client.callWithTimeout(tempDir,
                        mvn() + " -e -B zanata:push -Dzanata.pushType=both -Dzanata.userConfig="
                                + userConfigPath);

        assertThat(client.isPushSuccessful(output)).isTrue();

        EditorPage editorPage = verifyPluralPushedToEditor();

        File pullDir = Files.createTempDir();
        String pullDirPath = pullDir.getAbsolutePath();
        String command =
                mvn() + " -e -B zanata:pull -Dzanata.pullType=both -Dzanata.srcDir="
                        + pullDirPath + " -Dzanata.transDir=" + pullDirPath
                        + " -Dzanata.userConfig=" + userConfigPath;
        output = client.callWithTimeout(tempDir, command);
        assertThat(client.isPushSuccessful(output)).isTrue();

        // source round trip
        List<TextFlow> originalTextFlows =
                getTextFlows(new File(projectRootPath + "/pot/test.pot"));
        List<TextFlow> pulledTextFlows =
                getTextFlows(new File(pullDir, "test.pot"));
        assertThat(pulledTextFlows).isEqualTo(originalTextFlows);

        // translation round trip
        List<TextFlowTarget> originalTargets =
                getTextFlowTargets(new File(projectRootPath + "/pl/test.po"));
        List<TextFlowTarget> pulledTargets =
                getTextFlowTargets(new File(pullDir + "/pl/test.po"));
        assertThat(pulledTargets).isEqualTo(originalTargets);

        // translate on web UI and pull again
        editorPage.translateTargetAtRowIndex(0, "one aoeuaouaou")
                .saveAsFuzzyAtRow(0);


        client.callWithTimeout(tempDir, command);
        List<String> newContents =
                getTextFlowTargets(new File(pullDir + "/pl/test.po")).get(0)
                        .getContents();
        assertThat(newContents).contains("one aoeuaouaou");

    }

    private static EditorPage verifyPluralPushedToEditor() {
        // verify first message
        new LoginWorkFlow().signIn("admin", "admin");
        EditorPage editorPage = new BasicWorkFlow()
                .goToEditor("plurals", "master", "pl", "test");

        assertThat(editorPage.getMessageSourceAtRowIndex(0, Plurals.SourceSingular))
                .isEqualTo("One file removed");
        assertThat(editorPage.getMessageSourceAtRowIndex(0, Plurals.SourcePlural))
                .isEqualTo("%d files removed");
        // nplural for Polish is 3

        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0,
                Plurals.TargetSingular))
                .isEqualTo("1 aoeuaouaou");
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0,
                Plurals.TargetPluralOne))
                .isEqualTo("%d aoeuaouao");
        assertThat(editorPage.getBasicTranslationTargetAtRowIndex(0,
                Plurals.TargetPluralTwo))
                .isEqualTo("");

        return editorPage;
    }

    private static List<TextFlow> getTextFlows(File file)
            throws FileNotFoundException {
        return new PoReader2().extractTemplate(
                new InputSource(new FileInputStream(file)), LocaleId.EN_US,
                file.getName()).getTextFlows();
    }

    private static List<TextFlowTarget> getTextFlowTargets(File file)
            throws FileNotFoundException {
        return new PoReader2().extractTarget(
                new InputSource(new FileInputStream(file)))
                .getTextFlowTargets();
    }
}
