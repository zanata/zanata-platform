package org.zanata.search;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.TestFixture;
import com.google.common.base.Charsets;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMatcherTest {
    private HDocument document = null;
    private String resId = "abc";
    private String transMemorySource =
            "Do you know <div><some>you</some> will <strong>never</strong></div> walk alone?<p><i>Yes</i>, I do.</p>";
    private String transMemoryTarget =
            "<div><some>你</some><strong>永远不会</strong></div>一个人走你知道吗?<p> <i>是</i>, 我知道.";
    private HTextFlow transMemory = new HTextFlow(document, resId,
            transMemorySource);
    private HLocale targetLocale = TestFixture.setId(1L, new HLocale(new LocaleId("zh")));

    @Before
    public void setUp() {
        HTextFlowTarget transMemoryTranslation =
                new HTextFlowTarget(transMemory, targetLocale);
        transMemoryTranslation.setContent0(transMemoryTarget);
        transMemory.getTargets().put(targetLocale.getId(),
                transMemoryTranslation);
    }

    @Test
    public void canMatchSameStructureButDifferentTags() {
        // Given:
        String upcomingSource =
                "Do you know <span><other>you</other> will <bold>never</bold></span> walk alone?<para><o>Yes</o>, I do.</para>";
        HTextFlow upcomingMessage =
                new HTextFlow(document, resId, upcomingSource);
        TransMemoryMatcher matcher =
                new TransMemoryMatcher(upcomingMessage, transMemory,
                        targetLocale);


        // When:
        double similarityPercent = matcher.calculateSimilarityPercent();

        // Then:
        Assertions.assertThat(similarityPercent)
                .isEqualTo(100)
                .as("same structure but different tags can be matched as 100%");

        // When:
        String translation = matcher.translationFromTransMemory();

        // Then:
        Assertions
                .assertThat(translation)
                .isEqualTo(
                        "<span><other>你<bold>永远不会</bold></span>一个人走你知道吗?<br> <o>是</o>, 我知道.")
                .as("will replace translation from TM with correct tags");
    }

    @Test
    public void test() {
        Document doc = Jsoup.parseBodyFragment(
                "Do you know <some>you</some> will <strong>never</strong> walk alone?<p> <i>Yes</i>, I do.");
        List<Node> nodes = doc.body().childNodes();
        TextNode node = (TextNode) nodes.get(0);
        node.text("Yes I know");
        Document.OutputSettings outputSettings = new Document.OutputSettings()
                .charset(Charsets.UTF_8).indentAmount(0).prettyPrint(false);
        System.out.println(doc.outputSettings(outputSettings).body().html());
    }

}
