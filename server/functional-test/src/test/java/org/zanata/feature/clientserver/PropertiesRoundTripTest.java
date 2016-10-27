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

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.fedorahosted.openprops.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.TestFileGenerator;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.feature.clientserver.ProjectMaintainerTest.MAVEN_PLUGIN;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class PropertiesRoundTripTest extends ZanataTestCase {


    private ClientWorkFlow client = new ClientWorkFlow();
    private ZanataRestCaller restCaller;

    private File tempDir = Files.createTempDir();

    private String userConfigPath = ClientWorkFlow
            .getUserConfigPath("admin");

    @Before
    public void setUp() throws IOException {
        restCaller = new ZanataRestCaller();
        // generate a properties source
        Properties properties = new Properties();
        properties.setProperty("hello", "hello world");
        properties.setProperty("greeting", "this is from Huston");
        properties.setProperty("hey", "hey hey");
        File propertiesSource = new File(tempDir, "test.properties");
        properties.store(new FileWriter(propertiesSource), "comment");
    }

    @Feature(summary = "The maintainer user may push and pull properties files",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 139837)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canPushAndPullProperties() throws IOException,
            InterruptedException {
        restCaller.createProjectAndVersion("properties-test", "master",
                "properties");
        // generate a zanata.xml
        TestFileGenerator.generateZanataXml(new File(tempDir, "zanata.xml"),
                "properties-test", "master", "properties", Lists
                .newArrayList("pl"));
        List<String> output = client.callWithTimeout(tempDir,
                "mvn -B " + MAVEN_PLUGIN + ":push -Dzanata.srcDir=. " +
                "-Dzanata.userConfig=" + userConfigPath);

        assertThat(client.isPushSuccessful(output)).isTrue();

        EditorPage editorPage = verifyPushedToEditor();
        editorPage = editorPage.translateTargetAtRowIndex(2,
                "translation updated approved")
                .approveTranslationAtRow(2);

        editorPage.translateTargetAtRowIndex(1, "translation updated fuzzy")
                .saveAsFuzzyAtRow(1);

        output = client.callWithTimeout(tempDir,
                "mvn -B " + MAVEN_PLUGIN + ":pull " +
                "-Dzanata.userConfig=" + userConfigPath);

        assertThat(client.isPushSuccessful(output)).isTrue();
        File transFile = new File(tempDir, "test_pl.properties");
        assertThat(transFile.exists()).isTrue();
        Properties translations = new Properties();
        translations.load(new FileReader(transFile));
        assertThat(translations.size()).isEqualTo(1);
        assertThat(translations.getProperty("hey"))
                .isEqualTo("translation updated approved");

        // change on client side
        translations.setProperty("greeting", "translation updated on client");
        translations.store(new FileWriter(transFile), null);

        // push again
        client.callWithTimeout(tempDir,
                "mvn -B " + MAVEN_PLUGIN + ":push " +
                "-Dzanata.pushType=trans -Dzanata.srcDir=. -Dzanata.userConfig="
                + userConfigPath);

        final EditorPage editor =
                new BasicWorkFlow().goToEditor("properties-test",
                        "master", "pl", "test");
        assertThat(editor.getBasicTranslationTargetAtRowIndex(1))
                .isEqualTo("translation updated on client");
    }

    private static EditorPage verifyPushedToEditor() {
        new LoginWorkFlow().signIn("admin", "admin");
        EditorPage editorPage =
                new BasicWorkFlow().goToEditor("properties-test",
                        "master", "pl", "test");

        assertThat(editorPage.getMessageSourceAtRowIndex(0))
                .isEqualTo("hello world");
        assertThat(editorPage.getMessageSourceAtRowIndex(1))
                .isEqualTo("this is from Huston");
        assertThat(editorPage.getMessageSourceAtRowIndex(2))
                .isEqualTo("hey hey");

        return editorPage;
    }
}
