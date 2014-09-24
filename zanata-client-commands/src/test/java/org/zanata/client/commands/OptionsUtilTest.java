package org.zanata.client.commands;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.zanata.client.config.ZanataConfig;

public class OptionsUtilTest {

    private ConfigurableProjectOptions opts;
    private ZanataConfig config;

    @Before
    public void setUp() {
        opts = new ConfigurableProjectOptionsImpl() {
            @Override
            public ZanataCommand initCommand() {
                return null;
            }

            @Override
            public String getCommandName() {
                return "testCommand";
            }

            @Override
            public String getCommandDescription() {
                return "testing";
            }
        };
        config = new ZanataConfig();
    }

    @Test
    public void willApplyConfigFromFileIfNotSetInOptions() throws Exception {
        // Given: options are not set and exists in zanata config
        config.setSrcDir("a");
        config.setTransDir("b");
        config.setIncludes("*.properties");
        config.setExcludes("a.properties");

        // When:
        OptionsUtil.applySrcDirAndTransDirFromProjectConfig(opts, config);
        OptionsUtil.applyIncludesAndExcludesFromProjectConfig(opts, config);

        // Then:
        assertThat(opts.getSrcDir(), Matchers.equalTo(new File("a")));
        assertThat(opts.getTransDir(), Matchers.equalTo(new File("b")));
        assertThat(opts.getIncludes(), Matchers.contains("*.properties"));
        assertThat(opts.getExcludes(), Matchers.contains("a.properties"));
    }

    @Test
    public void willSetToDefaultValueIfNeitherHasValue() {
        OptionsUtil.applySrcDirAndTransDirFromProjectConfig(opts, config);

        assertThat(opts.getSrcDir(), Matchers.equalTo(new File(".")));
        assertThat(opts.getTransDir(), Matchers.equalTo(new File(".")));
    }

    @Test
    public void optionTakesPrecedenceOverConfig() {
        // Given: options are set in both places
        opts.setSrcDir(new File("pot"));
        opts.setTransDir(new File("."));
        opts.setIncludes("*.properties");
        opts.setExcludes("a.properties,b.properties");
        config.setSrcDir("a");
        config.setTransDir("b");
        config.setIncludes("b.b");
        config.setExcludes("e,f");

        // When:
        OptionsUtil.applySrcDirAndTransDirFromProjectConfig(opts, config);
        OptionsUtil.applyIncludesAndExcludesFromProjectConfig(opts, config);

        // Then:
        assertThat(opts.getSrcDir(), Matchers.equalTo(new File("pot")));
        assertThat(opts.getTransDir(), Matchers.equalTo(new File(".")));
        assertThat(opts.getIncludes(), Matchers.contains("*.properties"));
        assertThat(opts.getExcludes(),
                Matchers.contains("a.properties", "b.properties"));
    }

}
