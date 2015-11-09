package org.zanata.client.commands;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Warning;
import static org.zanata.client.commands.FileMappingRuleHandler.Placeholders.allHolders;
import static org.zanata.client.commands.Messages.get;

import java.io.File;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.ZanataConfig;
import com.google.common.collect.Lists;

public class OptionsUtilTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ConfigurableProjectOptions opts;
    private ZanataConfig config;
    @Mock
    private ConsoleInteractor console;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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

    @Test
    public void willWarnUserIfRuleSeemsWrong() {
        opts.setInteractiveMode(false);
        String rule = "{foo}/{bar}/{locale}";
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(rule)));
        OptionsUtil.checkPotentialMistakesInRules(opts, console);

        verify(console).printfln(Warning, get("unrecognized.variables"),
                allHolders(), rule);
    }

    @Test
    public void willAskUserToConfirmIfRuleSeemsWrongAndInInteractiveMode() {
        opts.setInteractiveMode(true);
        String rule = "{foo}/{bar}/{locale}";
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(rule)));
        OptionsUtil.checkPotentialMistakesInRules(opts, console);

        verify(console).printfln(Warning, get("unrecognized.variables"),
                allHolders(), rule);
        verify(console).printfln(Question, get("confirm.rule"));
    }

    @Test
    public void willThrowExceptionIfRuleIsInvalid() {
        expectedException.expect(IllegalStateException.class);
        String rule = "{filename}/{path}";
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(rule)));

        OptionsUtil.checkPotentialMistakesInRules(opts, console);

        verify(console).printfln(Warning, get("invalid.rule"), rule);
    }
}
