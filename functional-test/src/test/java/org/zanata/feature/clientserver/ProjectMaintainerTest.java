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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.common.LocaleId;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.TestFileGenerator.generateZanataXml;
import static org.zanata.util.TestFileGenerator.makePropertiesFile;
import static org.zanata.util.ZanataRestCaller.buildTextFlowTarget;
import static org.zanata.util.ZanataRestCaller.buildTranslationResource;
import static org.zanata.workflow.BasicWorkFlow.PROJECT_VERSION_TEMPLATE;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProjectMaintainerTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule rule = new SampleProjectRule();
    private ClientWorkFlow client = new ClientWorkFlow();
    private File projectRootPath = client.getProjectRootPath("plural");
    private String translatorConfig = ClientWorkFlow
            .getUserConfigPath("translator");
    private FilenameFilter propFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".properties");
        }
    };

    @Feature(summary = "A non-maintainer user may not push to a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 91146)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void nonProjectMaintainerCanNotPush() {
        // admin creates the project
        ZanataRestCaller adminRestCaller = new ZanataRestCaller();
        adminRestCaller.createProjectAndVersion("plurals", "master", "podir");

        // translator tries to push
        List<String> output =
                client.callWithTimeout(projectRootPath,
                        "mvn -B zanata:push -Dzanata.userConfig="
                                + translatorConfig);

        String joinedOutput = Joiner.on("\n").skipNulls().join(output);
        assertThat(joinedOutput).contains("Authorization check failed");
    }

    @Feature(summary = "The system will run CopyTrans when a push occurs",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 91869)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void pushTransAndCopyTransTest() {
        // translator creates the project and become maintainer
        ZanataRestCaller restCaller =
                new ZanataRestCaller("translator",
                        PropertiesHolder.getProperty(
                                Constants.zanataTranslatorKey.value()));
        restCaller.createProjectAndVersion("plurals", "master", "podir");
        List<String> output =
                client.callWithTimeout(projectRootPath,
                        "mvn -B zanata:push -Dzanata.copyTrans=false -Dzanata.userConfig="
                                + translatorConfig);

        assertThat(client.isPushSuccessful(output)).isTrue();

        new LoginWorkFlow().signIn("admin", "admin");
        VersionLanguagesPage versionPage =
                new BasicWorkFlow().goToPage(String.format(
                        PROJECT_VERSION_TEMPLATE, "plurals", "master"),
                        VersionLanguagesPage.class);
        assertThat(versionPage.getStatisticsForLocale("pl"))
                .contains("0.0%");

        // push trans
        client.callWithTimeout(
                projectRootPath,
                "mvn -B zanata:push -Dzanata.pushType=trans -Dzanata.copyTrans=false -Dzanata.userConfig="
                        + translatorConfig);

        versionPage.reload();
        assertThat(versionPage.getStatisticsForLocale("pl")).contains("6.0%");

        // create new version
        restCaller.createProjectAndVersion("plurals", "beta", "podir");
        File updatedZanataXml = new File(Files.createTempDir(), "zanata.xml");
        generateZanataXml(updatedZanataXml, "plurals", "beta", "podir",
                Lists.newArrayList("pl"));
        // push source and run copyTrans
        client.callWithTimeout(
                projectRootPath,
                "mvn -B zanata:push -Dzanata.pushType=source -Dzanata.copyTrans=true -Dzanata.userConfig="
                        + translatorConfig
                        + " -Dzanata.projectConfig="
                        + updatedZanataXml.getAbsolutePath());

        VersionLanguagesPage betaVersionPage =
                new BasicWorkFlow().goToPage(String.format(
                        PROJECT_VERSION_TEMPLATE, "plurals", "beta"),
                        VersionLanguagesPage.class);

        assertThat(betaVersionPage.getStatisticsForLocale("pl")).contains("6.0%");
    }

    @Feature(summary = "A maintainer user may pull translations from a project",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 136564)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void projectMaintainerPullTest() throws IOException {
        ZanataRestCaller restCaller = new ZanataRestCaller("translator",
                PropertiesHolder
                        .getProperty(Constants.zanataTranslatorKey.value()));
        File workDir = Files.createTempDir();
        String projectSlug = "pull-test";
        String iterationSlug = "master";
        String projectType = "properties";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);
        // generate a properties source
        makePropertiesFile(new File(workDir, "prop1.properties"), ImmutableMap
                .<String, String> builder().put("hello", "hello world").build());
        makePropertiesFile(new File(workDir, "prop2.properties"), ImmutableMap
                .<String, String> builder().put("greeting", "hey buddy")
                .build());

        // copy a pom file
        generateZanataXml(new File(workDir, "zanata.xml"), projectSlug,
                iterationSlug, projectType, Lists.newArrayList("pl"));

        client.callWithTimeout(workDir,
                "mvn -B org.zanata:zanata-maven-plugin:push -Dzanata.userConfig="
                        + translatorConfig);

        // only message1 has translation
        TranslationsResource translationsResource =
                buildTranslationResource(buildTextFlowTarget("hello",
                        "translated"));
        restCaller.postTargetDocResource(projectSlug, iterationSlug, "prop1",
                new LocaleId("pl"), translationsResource, "auto");

        // dryRun creates nothing
        File transDir = Files.createTempDir();
        client.callWithTimeout(workDir,
                "mvn -B org.zanata:zanata-maven-plugin:pull -DdryRun -Dzanata.userConfig="
                        + translatorConfig + " -Dzanata.transDir=" + transDir);
        assertThat(transDir.listFiles(propFilter)).isEmpty();

        // create skeletons is false will only pull translated files
        client.callWithTimeout(
                workDir,
                "mvn -B org.zanata:zanata-maven-plugin:pull -Dzanata.createSkeletons=false -Dzanata.userConfig="
                        + translatorConfig
                        + " -Dzanata.transDir="
                        + transDir.getAbsolutePath());

        assertThat(transDir.listFiles(propFilter)).contains(new File(
                transDir, "prop1_pl.properties"));

        // pull both
        client.callWithTimeout(
                workDir,
                "mvn -B org.zanata:zanata-maven-plugin:pull -Dzanata.pullType=both -Dzanata.userConfig="
                        + translatorConfig
                        + " -Dzanata.transDir="
                        + transDir.getAbsolutePath());

        assertThat(transDir.listFiles(propFilter)).contains(new File(transDir,
                        "prop1_pl.properties"));
        // @formatter:off
        assertThat(workDir.listFiles(propFilter)).contains(
                new File(workDir, "prop1.properties"),
                new File(workDir, "prop2.properties"));
        // @formatter:on
    }

}
