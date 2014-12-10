/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.client.integraion;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.MockServerRule;
import org.zanata.client.TestProjectGenerator;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.pull.RawPullCommand;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.commands.push.RawPushCommand;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.resource.ResourceMeta;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PushPullFileProjectITCase {
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
        pullOpts.setProjectType("file");

        ConfigurableProjectOptions pushOpts = mockServerRule.getPushOpts();
        pushOpts.getLocaleMapList().add(new LocaleMapping("zh-CN"));
        pushOpts.setProjectType("file");
    }

    @Test
    public void pushFileTypeProject() throws Exception {
        PushOptionsImpl opts = mockServerRule.getPushOpts();
        opts.setPushType("both");
        File baseDir =
                testProjectGenerator.getProjectBaseDir(ProjectType.File);
        log.debug("testing project is at: {}", baseDir);
        opts.setFileTypes("odt,ods");
        opts.setSrcDir(new File(baseDir, "src"));
        opts.setTransDir(baseDir);

        RawPushCommand pushCommand = mockServerRule.createRawPushCommand();

        pushCommand.run();

        mockServerRule.verifyPushRawFileSource(2);
        assertThat(mockServerRule.getDocIdCaptor().getAllValues(),
                contains("test-ods.ods", "test-odt.odt"));

        mockServerRule.verifyPushRawFileTranslation(1);
        assertThat(mockServerRule.getDocIdCaptor().getValue(),
                equalTo("test-odt.odt"));

    }

    @Test
    public void pushFileProjectUsingFileMapping() throws Exception {
        PushOptionsImpl opts = mockServerRule.getPushOpts();
        opts.setPushType("trans");
        File baseDir =
                testProjectGenerator.getProjectBaseDir(ProjectType.File);
        log.debug("testing project is at: {}", baseDir);
        opts.setFileTypes("odt,ods");
        opts.setSrcDir(new File(baseDir, "src"));
        opts.setTransDir(baseDir);
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(
                "{locale}/{path}/{filename}.{extension}")));

        RawPushCommand pushCommand = mockServerRule.createRawPushCommand();

        pushCommand.run();

        mockServerRule.verifyPushRawFileTranslation(1);
        assertThat(mockServerRule.getDocIdCaptor().getValue(),
                equalTo("test-odt.odt"));
    }

    @Test
    public void pullFileProject() throws Exception {
        PullOptionsImpl opts = mockServerRule.getPullOpts();
        opts.setPullType("both");
        File pullBaseDir = tempFolder.newFolder("file-pull-test");
        opts.setSrcDir(pullBaseDir);
        opts.setTransDir(pullBaseDir);
        log.debug("pull base dir is: {}", pullBaseDir);

        InputStream sourceFileStream =
                IOUtils.toInputStream("source content", Charsets.UTF_8);
        InputStream transFileStream =
                IOUtils.toInputStream("translation content", Charsets.UTF_8);
        ArrayList<ResourceMeta> remoteDocList =
                Lists.newArrayList(new ResourceMeta("test-ods.ods"),
                        new ResourceMeta("test-odt.odt"));

        RawPullCommand pullCommand = mockServerRule.createRawPullCommand(
                remoteDocList, sourceFileStream, transFileStream);

        pullCommand.run();

        assertThat(new File(pullBaseDir, "test-ods.ods").exists(), is(true));
        assertThat(new File(pullBaseDir, "test-odt.odt").exists(), is(true));
        assertThat(new File(pullBaseDir, "zh-CN/test-odt.odt").exists(), is(true));
        assertThat(new File(pullBaseDir, "zh-CN/test-ods.ods").exists(), is(true));
    }

    @Test
    public void pullFileProjectUsingFileMapping() throws Exception {
        PullOptionsImpl opts = mockServerRule.getPullOpts();
        opts.setPullType("trans");
        File pullBaseDir = tempFolder.newFolder("file-pull-test");
        opts.setSrcDir(pullBaseDir);
        opts.setTransDir(pullBaseDir);
        log.debug("pull base dir is: {}", pullBaseDir);
        // we define our own rule
        opts.setFileMappingRules(Lists.newArrayList(
                new FileMappingRule("**/*.odt", "{extension}/{path}/{locale}/{filename}.{extension}"),
                new FileMappingRule("**/*.ods", "{extension}/{locale_with_underscore}/{filename}.{extension}")
        ));

        InputStream sourceFileStream =
                IOUtils.toInputStream("source content", Charsets.UTF_8);
        InputStream transFileStream =
                IOUtils.toInputStream("translation content", Charsets.UTF_8);
        ArrayList<ResourceMeta> remoteDocList =
                Lists.newArrayList(new ResourceMeta("test-ods.ods"),
                        new ResourceMeta("test-odt.odt"));

        RawPullCommand pullCommand = mockServerRule.createRawPullCommand(
                remoteDocList, sourceFileStream, transFileStream);

        pullCommand.run();

        assertThat(new File(pullBaseDir, "odt/zh-CN/test-odt.odt").exists(), is(true));
        assertThat(new File(pullBaseDir, "ods/zh_CN/test-ods.ods").exists(), is(true));
    }
}

