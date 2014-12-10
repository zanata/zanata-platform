package org.zanata.client.integraion;

import java.io.File;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.MockServerRule;
import org.zanata.client.TestProjectGenerator;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PushPullGettextITCase {
    private static final Logger log = LoggerFactory
            .getLogger(PushPullGettextITCase.class);
    @Rule
    public MockServerRule mockServerRule = new MockServerRule();
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private TestProjectGenerator testProjectGenerator =
            new TestProjectGenerator();

    @Before
    public void setUp() {
        ConfigurableProjectOptions pullOpts = mockServerRule.getPullOpts();
        pullOpts.getLocaleMapList().add(new LocaleMapping("zh-CN"));
        pullOpts.setProjectType("gettext");

        ConfigurableProjectOptions pushOpts = mockServerRule.getPushOpts();
        pushOpts.getLocaleMapList().add(new LocaleMapping("zh-CN"));
        pushOpts.setProjectType("gettext");
    }

    @Test
    public void pushGettextProject() throws Exception {
        PushOptionsImpl opts = mockServerRule.getPushOpts();
        opts.setPushType("both");
        File baseDir =
                testProjectGenerator.getProjectBaseDir(ProjectType.Gettext);
        log.debug("testing project is at: {}", baseDir);
        opts.setSrcDir(new File(baseDir, "po"));
        opts.setTransDir(new File(baseDir, "po"));

        PushCommand pushCommand = mockServerRule.createPushCommand();

        pushCommand.run();

        mockServerRule.verifyPushSource();
        String docId = mockServerRule.getDocIdCaptor().getValue();
        assertThat(docId, equalTo("tar"));
        assertThat(mockServerRule.getExtensionCaptor().getValue(),
                Matchers.<Set> equalTo(new StringSet("gettext;comment")));

        Resource resource = mockServerRule.getResourceCaptor().getValue();
        assertThat(resource.getTextFlows(), hasSize(2));

        mockServerRule.verifyPushTranslation();
        LocaleId localeId = mockServerRule.getLocaleIdCaptor().getValue();
        assertThat(localeId, equalTo(new LocaleId("zh-CN")));

        TranslationsResource transResource =
                mockServerRule.getTransResourceCaptor().getValue();
        assertThat(transResource.getTextFlowTargets(), hasSize(2));
    }

    @Test
    public void pushGettextProjectUsingFileMapping() throws Exception {
        PushOptionsImpl opts = mockServerRule.getPushOpts();
        opts.setPushType("trans");
        File baseDir =
                testProjectGenerator.getProjectBaseDir(ProjectType.Gettext);
        log.debug("testing project is at: {}", baseDir);
        opts.setSrcDir(new File(baseDir, "po"));
        opts.setTransDir(new File(baseDir, "po"));
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(
                "{path}/{locale_with_underscore}.po")));

        PushCommand pushCommand = mockServerRule.createPushCommand();

        pushCommand.run();

        mockServerRule.verifyPushTranslation();
        LocaleId localeId = mockServerRule.getLocaleIdCaptor().getValue();
        assertThat(localeId, equalTo(new LocaleId("zh-CN")));

        TranslationsResource transResource =
                mockServerRule.getTransResourceCaptor().getValue();
        assertThat(transResource.getTextFlowTargets(), hasSize(2));
    }

    @Test
    public void pullGettextProject() throws Exception {
        PullOptionsImpl opts = mockServerRule.getPullOpts();
        opts.setPullType("both");
        File pullBaseDir = tempFolder.newFolder("gettext-pull-test");
        opts.setSrcDir(pullBaseDir);
        opts.setTransDir(pullBaseDir);

        Resource resourceOnServer = new Resource("tar");
        resourceOnServer.getTextFlows().add(
                new TextFlow("hello", LocaleId.EN_US, "hello world"));
        TranslationsResource transResourceOnServer = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget("hello");
        target.getExtensions(true);
        target.setContents("hello world translated");
        transResourceOnServer.getTextFlowTargets().add(target);

        PullCommand pullCommand = mockServerRule.createPullCommand(
                Lists.newArrayList(new ResourceMeta("tar")), resourceOnServer,
                transResourceOnServer);

        pullCommand.run();

        assertThat(new File(pullBaseDir, "tar.pot").exists(), is(true));
        assertThat(new File(pullBaseDir, "zh_CN.po").exists(), is(true));
    }

    @Test
    public void pullGettextProjectUsingFileMapping() throws Exception {
        PullOptionsImpl opts = mockServerRule.getPullOpts();
        opts.setPullType("trans");
        File pullBaseDir = tempFolder.newFolder("gettext-pull-test");
        opts.setSrcDir(pullBaseDir);
        opts.setTransDir(pullBaseDir);
        // we define our own rule
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(
                "{filename}_{locale}.po")));

        Resource resourceOnServer = new Resource("tar");
        resourceOnServer.getTextFlows().add(
                new TextFlow("hello", LocaleId.EN_US, "hello world"));
        TranslationsResource transResourceOnServer = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget("hello");
        target.getExtensions(true);
        target.setContents("hello world translated");
        transResourceOnServer.getTextFlowTargets().add(target);

        PullCommand pullCommand = mockServerRule.createPullCommand(
                Lists.newArrayList(new ResourceMeta("tar")), resourceOnServer,
                transResourceOnServer);

        pullCommand.run();

        assertThat(new File(pullBaseDir, "tar.pot").exists(), is(false));
        assertThat(new File(pullBaseDir, "zh_CN.po").exists(), is(false));
        assertThat(new File(pullBaseDir, "tar_zh-CN.po").exists(), is(true));
    }

}

