package org.zanata.search;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMatcherTest {
    private HDocument document = null;
    private String resId = "abc";
    private String transMemorySource =
            "Do you know <some>you</some> will <strong>never</strong> walk alone?<p> <i>Yes</i>, I do.";
    private String transMemoryTarget =
            "<some>你<strong>永远不会</strong>一个人走你知道吗?<p> <i>是</i>, 我知道.";
    private HTextFlow tranMemory = new HTextFlow(document, resId,
            transMemorySource);
    private Long targetLocaleId = 1L;
    private TransMemoryMatcher matcher;

    @Before
    public void setUp() {
        matcher = new TransMemoryMatcher();
        HTextFlowTarget transMemoryTranslation =
                new HTextFlowTarget(tranMemory, new HLocale(
                        new LocaleId("zh")));
        transMemoryTranslation.setContent0(transMemoryTarget);
        tranMemory.getTargets().put(targetLocaleId,
                transMemoryTranslation);
    }

    @Test
    public void canMatchSameStructureButDifferentTags() {
        // Given:
        String upcomingSource =
                "Do you know <other>you</other> will <bold>never</bold> walk alone?<br> <o>Yes</o>, I do.";
        HTextFlow upcomingMessage =
                new HTextFlow(document, resId, upcomingSource);

        // When:
        int similarityPercent =
                matcher.calculateSimilarityPercent(upcomingMessage, tranMemory);
        String translation = matcher.translationFromTransMemory(tranMemory);

        // Then:
        Assertions.assertThat(similarityPercent)
                .isEqualTo(100)
                .as("same structure but different tags can be matched as 100%");
        Assertions
                .assertThat(translation)
                .isEqualTo(
                        "<other>你<bold>永远不会</bold>一个人走你知道吗?<br> <o>是</o>, 我知道.")
                .as("will replace translation from TM with correct tags");
    }

}
