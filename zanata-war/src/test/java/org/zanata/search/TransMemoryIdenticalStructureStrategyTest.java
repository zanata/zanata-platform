package org.zanata.search;

import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

public class TransMemoryIdenticalStructureStrategyTest {
    private TransMemoryIdenticalStructureStrategy strategy;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void sameStructureCanUseThisStrategy() {
        strategy =
                new TransMemoryIdenticalStructureStrategy(
                        Jsoup.parseBodyFragment(
                                "you <a>are my <b>sunshine</b> and </a> air.")
                                .body(),
                        Jsoup.parseBodyFragment(
                                "you <aa>are my <bb>sunshine</bb> and </aa> air.")
                                .body(), "你<aa>是我的<bb>阳光</bb>和</aa>空气");

        Assertions.assertThat(strategy.canUse()).isTrue();
        Assertions.assertThat(strategy.translationFromTransMemory()).isEqualTo(
                "你<a>是我的<b>阳光</b>和</a>空气");
    }

    @Test
    public void sameStructureWithAttributesInTagCanUseThisStrategy() {
        strategy =
                new TransMemoryIdenticalStructureStrategy(
                        Jsoup.parseBodyFragment(
                                "you <a>are my <b id=1 class='pretty'>sunshine</b> and </a> air.")
                                .body(),
                        Jsoup.parseBodyFragment(
                                "you <aa>are my <bb>sunshine</bb> and </aa> air.")
                                .body(), "你<aa>是我的<bb>阳光</bb>和</aa>空气");

        Assertions.assertThat(strategy.canUse()).isTrue();

        Assertions.assertThat(strategy.translationFromTransMemory())
                .isNotEqualTo(
                        "你<a>是我的<b id=1 class='pretty'>阳光</b>和</a>空气");
        Assertions.assertThat(strategy.translationFromTransMemory()).isEqualTo(
                "你<a>是我的<b id=\"1\" class=\"pretty\">阳光</b>和</a>空气");
    }

    @Test
    public void differentStructureCanUseThisStrategy() {
        strategy =
                new TransMemoryIdenticalStructureStrategy(
                        Jsoup.parseBodyFragment(
                                "<c>you <a>are my <b>sunshine</b> and </a> air.</c>")
                                .body(),
                        Jsoup.parseBodyFragment(
                                "you <aa>are my <bb>sunshine</bb> and </aa> air.")
                                .body(), "你<aa>是我的<bb>阳光</bb>和</aa>空气");

        Assertions.assertThat(strategy.canUse()).isFalse();
    }
}
