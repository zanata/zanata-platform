package org.zanata.feature.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;

import java.util.List;
import java.util.concurrent.Callable;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditorFilterMessagesTest extends ZanataTestCase {
    private final String document = "messages";
    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Before
    public void setUp() {
        ZanataRestCaller restCaller = new ZanataRestCaller();
        Resource sourceResource =
                buildSourceResource(document,
                        buildTextFlow("res1", "hello world"),
                        buildTextFlow("res2", "greetings"),
                        buildTextFlow("res3", "hey"));
        restCaller.postSourceDocResource("about-fedora", "master",
                sourceResource, false);
        new LoginWorkFlow().signIn("admin", "admin");
    }

    @Feature(summary = "The user can filter translation entries using more " +
            "than one search term",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canFilterByMultipleFields() {
        EditorPage editorPage =
                new BasicWorkFlow().goToEditor("about-fedora", "master", "fr",
                        document);
        assertThat(editorPage.getMessageSources()).containsExactly(
                "hello world", "greetings", "hey");
        final EditorPage page = editorPage.inputFilterQuery("resource-id:res2");

        editorPage.waitFor(new Callable<Iterable<? extends String>>() {
            @Override
            public List<String> call() throws Exception {
                return page.getMessageSources();
            }
        }, Matchers.contains("greetings"));
    }

    @Feature(summary = "The user may save the filter url for later use",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void editorFilterIsBookmarkable() {
        String urlForEditor =
                String.format(BasicWorkFlow.EDITOR_TEMPLATE, "about-fedora",
                        "master", "fr", document);
        String urlWithFilterCondition =
                urlForEditor + ";search:hello%20w;resid:res1";
        EditorPage editorPage =
                new BasicWorkFlow().goToPage(urlWithFilterCondition,
                        EditorPage.class);

        assertThat(editorPage.getMessageSources()).containsExactly("hello world");
        assertThat(editorPage.getFilterQuery().trim()).isEqualTo("text:hello w resource-id:res1");
    }
}
