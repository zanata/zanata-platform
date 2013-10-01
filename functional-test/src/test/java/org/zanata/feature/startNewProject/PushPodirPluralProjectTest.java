/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.feature.startNewProject;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.LoggingTooltipExtension;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.workflow.ClientPushWorkFlow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ ScreenshotExtension.class, TimestampFormatterExtension.class,
        CustomResourceExtension.class, LoggingTooltipExtension.class })
@Category(ConcordionTest.class)
public class PushPodirPluralProjectTest {
    private final static Logger tooltipLog = Logger
            .getLogger(PushPodirPluralProjectTest.class.getName());

    @Extension
    public ConcordionExtension extension = new LoggingTooltipExtension(
            PushPodirPluralProjectTest.class.getName(), Level.INFO, false);

    private ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
    private File projectRootPath;

    public String getUserConfigPath() {
        return ClientPushWorkFlow.getUserConfigPath("admin");
    }

    public String getUserConfigContent(String path) throws IOException {
        List<String> lines =
                Files.readLines(new File(path), Charset.defaultCharset());
        return Joiner.on("\n").join(lines);
    }

    public String getProjectLocation(String project) {
        projectRootPath = clientPushWorkFlow.getProjectRootPath(project);
        tooltipLog.info("project root path:"
                + projectRootPath.getAbsolutePath());
        return projectRootPath.getAbsolutePath();
    }

    public List<String> push(String command, String configPath)
            throws Exception {
        return clientPushWorkFlow.callWithTimeout(projectRootPath, command
                + configPath);
    }

    public boolean isPushSuccessful(List<String> output) {
        return clientPushWorkFlow.isPushSuccessful(output);
    }

    public String resultByLines(List<String> output) {
        return Joiner.on("\n").join(output);
    }

}
