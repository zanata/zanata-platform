package org.zanata.client.commands;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TransFileResolverTest {

    private TransFileResolver resolver;
    private ConfigurableProjectOptions opts;

    @Before
    public void setUp() {
        opts = new PushOptionsImpl();
        resolver = new TransFileResolver(opts);
    }

    @Test
    public void canGetTransFileUsingRule() {
        opts.setTransDir(new File("."));
        opts.setProjectType("podir");
        opts.setFileMappingRules(Lists.newArrayList(
            new FileMappingRule("**/*.pot",
                "{path}/{locale_with_underscore}.po"),
            new FileMappingRule("**/*.properties",
                "{path}/{filename}_{locale_with_underscore}.{extension}")));
        File gettext =
            resolver.resolveTransFile(DocNameWithExt.from(
                    "gcc/po/gcc.pot"), new LocaleMapping("de-DE"), Optional
                .<String>absent());

        assertThat(gettext.getPath(), equalTo("./gcc/po/de_DE.po"));

        File prop = resolver
            .resolveTransFile(DocNameWithExt.from(
                            "src/main/resources/messages.properties"),
                    new LocaleMapping("zh"), Optional.<String>absent());
        assertThat(prop.getPath(), equalTo(
            "./src/main/resources/messages_zh.properties"));
    }

    @Test
    public void canGetTransFileUsingProjectTypeIfNoRuleIsApplicable() {
        opts.setTransDir(new File("."));
        opts.setProjectType("file");
        File noMatching = resolver
                .resolveTransFile(DocNameWithExt.from(
                        "doc/marketing.odt"), new LocaleMapping("ja"), Optional.<String>absent());
        assertThat(noMatching.getPath(), equalTo("./ja/doc/marketing.odt"));
    }

}
