package org.zanata.feature.clientserver;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.common.LocaleId;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.ClientWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import static org.hamcrest.MatcherAssert.assertThat;
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
public class ProjectMaintainerTest {
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

    /**
     * TCMS test case <a
     * href="https://tcms.engineering.redhat.com/case/91146/">91146</a>
     */
    @Test
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
        assertThat(joinedOutput,
                Matchers.containsString("Authorization check failed"));
    }

    /**
     * TCMS test case <a
     * href="https://tcms.engineering.redhat.com/case/91869/">91869</a>
     */
    @Test
    public void pushTransAndCopyTransTest() {
        // translator creates the project and become maintainer
        ZanataRestCaller restCaller =
                new ZanataRestCaller("translator",
                        "d83882201764f7d339e97c4b087f0806");
        restCaller.createProjectAndVersion("plurals", "master", "podir");
        List<String> output =
                client.callWithTimeout(projectRootPath,
                        "mvn -B zanata:push -Dzanata.copyTrans=false -Dzanata.userConfig="
                                + translatorConfig);

        assertThat(client.isPushSuccessful(output), Matchers.is(true));

        new LoginWorkFlow().signIn("admin", "admin");
        ProjectVersionPage versionPage =
                new BasicWorkFlow().goToPage(String.format(
                        PROJECT_VERSION_TEMPLATE, "plurals", "master"),
                        ProjectVersionPage.class);
        assertThat(versionPage.getStatisticsForLocale("pl"),
                Matchers.containsString("0.0%"));

        // push trans
        client.callWithTimeout(
                projectRootPath,
                "mvn -B zanata:push -Dzanata.pushType=trans -Dzanata.copyTrans=false -Dzanata.userConfig="
                        + translatorConfig);

        versionPage.reload();
        assertThat(versionPage.getStatisticsForLocale("pl"),
                Matchers.containsString("6.0%"));

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

        ProjectVersionPage betaVersionPage =
                new BasicWorkFlow().goToPage(String.format(
                        PROJECT_VERSION_TEMPLATE, "plurals", "beta"),
                        ProjectVersionPage.class);

        assertThat(betaVersionPage.getStatisticsForLocale("pl"),
                Matchers.containsString("6.0%"));
    }

    /**
     * TCMS test case <a
     * href="https://tcms.engineering.redhat.com/case/136564/">136564</a>
     */
    @Test
    public void projectMaintainerPullTest() throws IOException {
        ZanataRestCaller restCaller =
                new ZanataRestCaller("translator",
                        "d83882201764f7d339e97c4b087f0806");
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
                new LocaleId("pl"), translationsResource);

        // dryRun creates nothing
        File transDir = Files.createTempDir();
        client.callWithTimeout(workDir,
                "mvn -B org.zanata:zanata-maven-plugin:pull -DdryRun -Dzanata.userConfig="
                        + translatorConfig + " -Dzanata.transDir=" + transDir);
        assertThat(transDir.listFiles(propFilter), Matchers.arrayWithSize(0));

        // create skeletons is false will only pull translated files
        client.callWithTimeout(
                workDir,
                "mvn -B org.zanata:zanata-maven-plugin:pull -Dzanata.createSkeletons=false -Dzanata.userConfig="
                        + translatorConfig
                        + " -Dzanata.transDir="
                        + transDir.getAbsolutePath());

        assertThat(transDir.listFiles(propFilter), Matchers.arrayContaining(new File(
                transDir, "prop1_pl.properties")));

        // pull both
        client.callWithTimeout(
                workDir,
                "mvn -B org.zanata:zanata-maven-plugin:pull -Dzanata.pullType=both -Dzanata.userConfig="
                        + translatorConfig
                        + " -Dzanata.transDir="
                        + transDir.getAbsolutePath());

        assertThat(transDir.listFiles(propFilter),
                Matchers.arrayContainingInAnyOrder(new File(transDir,
                        "prop1_pl.properties")));
        // @formatter:off
        assertThat(workDir.listFiles(propFilter), Matchers.arrayContainingInAnyOrder(
                new File(workDir, "prop1.properties"),
                new File(workDir, "prop2.properties")));
        // @formatter:on
    }

}
