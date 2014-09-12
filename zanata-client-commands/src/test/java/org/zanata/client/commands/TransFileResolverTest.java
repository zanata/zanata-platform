package org.zanata.client.commands;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.zanata.client.commands.init.InitOptionsImpl;
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
        // we choose init option as the implementation just because it's simple
        opts = new InitOptionsImpl();
        resolver = new TransFileResolver(opts);
    }

    @Test
    public void canGetTransFileUsingRule() {
        opts.setTransDir(new File("."));
        opts.setFileMappingRules(Lists.newArrayList(
            new FileMappingRule("**/*.pot",
                "{path}/{locale_with_underscore}.po"),
            new FileMappingRule("**/*.properties",
                "{path}/{filename}_{locale_with_underscore}.{extension}")));
        Optional<File> gettext =
            resolver.getTransFile("gcc/po/gcc.pot", new LocaleMapping("de-DE"));

        assertThat(gettext.get().getPath(), equalTo("./gcc/po/de_DE.po"));

        Optional<File> prop = resolver
            .getTransFile("src/main/resources/messages.properties",
                new LocaleMapping("zh"));
        assertThat(prop.get().getPath(), equalTo(
            "./src/main/resources/messages_zh.properties"));

        Optional<File> noMatching = resolver
            .getTransFile("doc/marketting.odt", new LocaleMapping("ja"));
        assertThat(noMatching.isPresent(), equalTo(false));
    }

}