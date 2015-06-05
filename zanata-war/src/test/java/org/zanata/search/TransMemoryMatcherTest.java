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
    private HTextFlow transMemory;
    private HLocale targetLocale = TestFixture.setId(1L, new HLocale(new LocaleId("zh")));
    private TransMemoryMatcher matcher;

    @Before
    public void setUp() {
        transMemory = null;
    }

    public void givenTransMemory(String sourceContent, String targetContent) {
        transMemory = new HTextFlow(document, resId,
                sourceContent);
        HTextFlowTarget transMemoryTranslation =
                new HTextFlowTarget(transMemory, targetLocale);
        transMemoryTranslation.setContent0(targetContent);
        transMemory.getTargets().put(targetLocale.getId(),
                transMemoryTranslation);
    }

    @Test
    public void canMatchSameStructureButDifferentTags() {
        // Given:
        givenTransMemory(
                "Do you know <div><some>you</some> will <strong>never</strong></div> walk alone?",
                "你知道吗<div><some>你</some>将<strong>永远不会</strong></div>一个人走?");
        String upcomingSource =
                "Do you know <span><other>you</other> will <bold>never</bold></span> walk alone?";
        matcher = givenUpcomingSourceToMatch(upcomingSource);


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
                        "你知道吗<span><other>你</other>将<bold>永远不会</bold></span>一个人走?")
                .as("will replace translation from TM with correct tags");
    }

    public TransMemoryMatcher givenUpcomingSourceToMatch(
            String upcomingSource) {
        HTextFlow upcomingMessage =
                new HTextFlow(document, resId, upcomingSource);
        return new TransMemoryMatcher(upcomingMessage, transMemory,
                targetLocale);
    }

    @Test
    public void canMatchSameStructureButDifferentTagsPlusTranslationTextSwappedLocation() {
        // Given:
        givenTransMemory(
                "Do you know <div><some>you</some> will <strong>never</strong></div> walk alone?",
                "<div><some>你</some><strong>永远不会</strong></div>一个人走你知道吗?");
        String upcomingSource =
                "Do you know <span><other>you</other> will <bold>never</bold></span> walk alone?";
        matcher =
                givenUpcomingSourceToMatch(upcomingSource);


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
                        "<span><other>你</other><bold>永远不会</bold></span>一个人走你知道吗?")
                .as("will replace translation from TM with correct tags");
    }

    @Test
    public void canHandleIdenticalTextTokensInSourceWithDifferentTranslation() {
        // Given: two text tokens are identical in source, e.g. "<some>good</some>"
        givenTransMemory(
                "How <some>good</some> are <strong>you</strong>? I am <some>good</some>.",
                "<some>好</some><strong>你</strong>吗？ 我<some>不错</some>。");
        matcher = givenUpcomingSourceToMatch(
                "How <other>good</other> are <bold>you</bold>? I am <other>good</other>.");

        // When:
        String translation = matcher.translationFromTransMemory();

        // Then:
        Assertions.assertThat(translation)
                .isEqualTo(
                        "<other>好</other><bold>你</bold>吗？ 我<other>不错</other>。");
    }

    @Test
    public void extraTagsOutsideText() {
        givenTransMemory(
                "I <b>see</b> you",
                "我<b>看见</b>你");
        matcher = givenUpcomingSourceToMatch(
                "I <a><bb>see</bb></a> you");
        String translation = matcher.translationFromTransMemory();
        Assertions.assertThat(translation)
                .isEqualTo("我<a><bb>看见</bb></a>你");
    }

    @Test
    public void extraTagsInsideText() {
        givenTransMemory(
                "I <b>see</b> you",
                "我<b>看见</b>你");
        matcher = givenUpcomingSourceToMatch(
                "I <bb><a>see</a></bb> you");
        String translation = matcher.translationFromTransMemory();
        Assertions.assertThat(translation)
                .isEqualTo("我<bb><a>看见</a></bb>你");
    }

    @Test
    public void extraTagsOutsideOfTags() {
        givenTransMemory(
                "I <b>can <c>not</c> believe</b> you",
                "我<b><c>不</c>能相信</b>你");
        matcher = givenUpcomingSourceToMatch(
                "I <a><bb>can <cc>not</cc> believe</bb></a> you");
        String translation = matcher.translationFromTransMemory();
        Assertions.assertThat(translation)
                .isEqualTo("我<a><bb><cc>不</cc>能相信</bb></a>你");
    }

    @Test
    public void tagsSwappedLocation() {
        // Given: elements swapped location
        givenTransMemory(
                "How <some>good</some> are <strong>you</strong>? I am <some>good</some>.",
                "<strong>你</strong><some>好</some>吗？ 我<some>不错</some>。");
        matcher = givenUpcomingSourceToMatch(
                "How <other>good</other> are <bold>you</bold>? I am <other>good</other>.");

        // When:
        double similarityPercent = matcher.calculateSimilarityPercent();

        // Then: we cannot handle element swapped location
        Assertions.assertThat(similarityPercent)
                .isEqualTo(99);
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

// upcoming:
//         - source:
//              Do you know <other>you</other> will <bold>never</bold> walk <span>alone</span>? <d> <o>Yes</o>, I do.</d>
//       TM:
//         - source:
//              Do you know <some>you</some> will <strong>never</strong> walk <some>alone<some>?<p> <i>Yes</i>, I do.</p>
//         - target:
//              DO YOU KNOW <some>YOU</some> WILL <strong>NEVER</strong> WALK <some>ALONE<some>?<p> <i>YES</i>, I DO.</p>    O (only if upcoming source has same tag on "you" and "alone")
//              <strong>NEVER</strong> WILL <some>YOU</some>DO YOU KNOW WALK <some>ALONE</some>?<p> <i>YES</i>, I DO.</p>    X
//              <some>YOU</some> WILL DO YOU KNOW <strong>NEVER</strong>WALK <some>ALONE</some>?<p> <i>YES</i>, I DO.</p>    X
    }

}
