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
import org.zanata.feature.DetailedTest;
import org.zanata.feature.ZanataTestCase;
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

    @Test
    public void canFilterByMultipleFields() {
        EditorPage editorPage =
                new BasicWorkFlow().goToEditor("about-fedora", "master", "fr",
                        document).setSyntaxHighlighting(false);
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

    @Test
    public void editorFilterIsBookmarkable() {
        String urlForEditor =
                String.format(BasicWorkFlow.EDITOR_TEMPLATE, "about-fedora",
                        "master", "fr", document);
        String urlWithFilterCondition =
                urlForEditor + ";search:resource-id!cres1%20text!c%22hello%20w%22";
        EditorPage editorPage =
                new BasicWorkFlow().goToPage(urlWithFilterCondition,
                        EditorPage.class).setSyntaxHighlighting(false);

        assertThat(editorPage.getMessageSources()).containsExactly("hello world");
        assertThat(editorPage.getFilterQuery()).isEqualTo("resource-id:res1 text:\"hello w\"");
    }
}
