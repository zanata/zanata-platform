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
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.TestProjectGenerator;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.commands.push.RawPushCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FileProjectITCase {
    private static final Logger log =
            LoggerFactory.getLogger(FileProjectITCase.class);
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private TestProjectGenerator testProjectGenerator =
            new TestProjectGenerator();
    private PushOptionsImpl opts;

    @Before
    public void setUp() throws Exception {
        opts = new PushOptionsImpl();
        TestProjectGenerator.ServerInstance instance =
                TestProjectGenerator.ServerInstance.FunctionalTestCargo;
        opts.setUrl(instance.getURL());
        opts.setUsername(instance.getUsername());
        opts.setKey(instance.getKey());
        testProjectGenerator.ensureProjectOnServer(opts,
                ProjectType.File, instance);
        opts.setLocaleMapList(new LocaleList());
        opts.getLocaleMapList().add(new LocaleMapping("zh-CN"));
        opts.setBatchMode(true);
    }

    @Test
    public void manualTest() throws IOException {
        File baseDir =
                testProjectGenerator.getProjectBaseDir(ProjectType.File);
        log.debug("testing project is at: {}", baseDir);
        opts.setFileTypes("odt,ods");
        opts.setSrcDir(new File(baseDir, "src"));
        opts.setTransDir(baseDir);
        opts.setProj(testProjectGenerator.sampleProjectSlug(baseDir));
        opts.setProjectVersion(testProjectGenerator.sampleIterationSlug());
        opts.setProjectType("file");
        opts.setFileTypes("odt");

        RawPushCommand pushCommand = new RawPushCommand(opts);

        pushCommand.run();
    }
}
