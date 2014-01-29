package org.zanata.feature.concurrentedit;

import java.util.concurrent.Callable;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.common.LocaleId;
import org.zanata.feature.DetailedTest;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;
import static org.zanata.util.ZanataRestCaller.buildTextFlowTarget;
import static org.zanata.util.ZanataRestCaller.buildTranslationResource;
import static org.zanata.workflow.BasicWorkFlow.EDITOR_TEMPLATE;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
@Slf4j
public class ConcurrentEditTest {
    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private ZanataRestCaller restCaller;

    @Before
    public void setUp() {
        restCaller = new ZanataRestCaller("admin");
    }

    @Test
    public void editorReceivesRestServiceResults() {
        // create project and push source
        String projectSlug = "base";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);

        String docId = "test.pot";
        Resource sourceResource =
                buildSourceResource(docId,
                        buildTextFlow("res1", "hello world"),
                        buildTextFlow("res2", "greetings"));
        restCaller.postSourceDocResource(projectSlug, iterationSlug,
                sourceResource, false);

        // open editor
        // open editor
        new LoginWorkFlow().signIn("admin", "admin");
        // webTrans
        final EditorPage editorPage =
                new BasicWorkFlow().goToPage(String.format(
                        EDITOR_TEMPLATE, "base", "master", "pl",
                        "test.pot"), EditorPage.class);

        String translation = editorPage.getTranslationTargetAtRowIndex(0);
        // for some reason getText() will return one space in it
        assertThat(translation.trim(), Matchers.isEmptyString());

        // push target
        TranslationsResource translationsResource =
                buildTranslationResource(
                        buildTextFlowTarget("res1", "hello world translated"));
        restCaller.postTargetDocResource(projectSlug, iterationSlug, docId,
                new LocaleId("pl"), translationsResource);

        // REST push broadcast event to editor
        editorPage.waitFor(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return editorPage.getTranslationTargetAtRowIndex(0);
            }
        }, Matchers.equalTo("hello world translated"));
    }

    @Test
    public void editorReceivesCopyTransResults() throws Exception {
        // create project and populate master version
        String projectSlug = "base";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);

        String docId = "test.pot";
        Resource sourceResource =
                buildSourceResource(docId,
                        buildTextFlow("res1", "hello world"),
                        buildTextFlow("res2", "greetings"));
        restCaller.postSourceDocResource(projectSlug, iterationSlug,
                sourceResource, false);

        TranslationsResource translationsResource =
                buildTranslationResource(
                        buildTextFlowTarget("res1", "hello world translated"));
        restCaller.postTargetDocResource(projectSlug, iterationSlug, docId,
                new LocaleId("pl"), translationsResource);

        // create and push source but disable copyTrans
        restCaller.createProjectAndVersion(projectSlug, "beta", projectType);
        restCaller.postSourceDocResource(projectSlug, "beta", sourceResource,
                false);

        // open editor
        new LoginWorkFlow().signIn("admin", "admin");
        // webTrans
        final EditorPage editorPage =
                new BasicWorkFlow().goToPage(String.format(
                        EDITOR_TEMPLATE, "base", "beta", "pl",
                        "test.pot"), EditorPage.class);

        String translation = editorPage.getTranslationTargetAtRowIndex(0);
        // for some reason getText() will return one space in it
        assertThat(translation.trim(), Matchers.isEmptyString());

        // run copyTrans
        restCaller.runCopyTrans(projectSlug, "beta", docId);

        // copyTrans broadcast event to editor
        editorPage.waitFor(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return editorPage.getTranslationTargetAtRowIndex(0);
            }
        }, Matchers.equalTo("hello world translated"));
    }

}
