package org.zanata.client.commands.pull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zanata.client.TestUtils.createAndAddLocaleMapping;

public class XmlStrategyPullTest {
    private XmlStrategy strategy;
    private PullOptionsImpl opts;

    @Before
    public void setUp() {
        opts = new PullOptionsImpl();
        opts.setLocaleMapList(new LocaleList());
        opts.setProjectType("xml");
        strategy = new XmlStrategy(opts);
    }

    @Test
    public void canGetTransFileWithoutMappingRule() {
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
            Optional.<String>absent(), opts);
        LocaleMapping zhMapping =
            createAndAddLocaleMapping("zh-CN",
                Optional.of("zh-Hans"),
                opts);

        File deTransFile =
            strategy.getTransFileToWrite("foo/message", deMapping);

        assertThat(deTransFile,
            equalTo(new File(opts.getTransDir(), "foo/message_de.xml")));

        File zhTransFile =
            strategy.getTransFileToWrite("foo/message", zhMapping);
        assertThat(zhTransFile, equalTo(
            new File(opts.getTransDir(), "foo/message_zh_Hans.xml")));

    }

    @Test
    public void canGetTransFileWithMappingRule() {
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
            Optional.<String>absent(), opts);
        LocaleMapping zhMapping =
            createAndAddLocaleMapping("zh-CN",
                Optional.of("zh-Hans"),
                opts);
        opts.setFileMappingRules(Lists.newArrayList(
            new FileMappingRule(
                "{path}/{filename}_{locale_with_underscore}.{extension}")));

        File deTransFile =
            strategy.getTransFileToWrite("foo/message", deMapping);

        assertThat(deTransFile,
            equalTo(new File(opts.getTransDir(), "foo/message_de.xml")));

        File zhTransFile =
            strategy.getTransFileToWrite("foo/message", zhMapping);
        assertThat(zhTransFile, equalTo(
            new File(opts.getTransDir(), "foo/message_zh_Hans.xml")));

    }

}
