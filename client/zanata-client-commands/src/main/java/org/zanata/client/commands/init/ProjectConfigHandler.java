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
package org.zanata.client.commands.init;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.zanata.client.commands.ConsoleInteractor;
import com.google.common.annotations.VisibleForTesting;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Confirmation;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Warning;
import static org.zanata.client.commands.Messages.get;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class ProjectConfigHandler {
    private final ConsoleInteractor consoleInteractor;
    private final InitOptions opts;
    private File backup;

    ProjectConfigHandler(ConsoleInteractor consoleInteractor, InitOptions opts) {
        this.consoleInteractor = consoleInteractor;
        this.opts = opts;
    }

    /**
     * If there's an existing zanata.xml, ask the user if they want to proceed,
     * back it up and continue (tell them where it is)
     *
     * @throws IOException
     */
    @VisibleForTesting
    protected void handleExistingProjectConfig() throws IOException {
        File projectConfig = getOpts().getProjectConfig();
        if (projectConfig.exists()) {
            consoleInteractor
                    .printfln(Warning, get("project.config.exists"))
                    .printf(Question, get("continue.yes.no"));
            consoleInteractor.expectYes();
            // back up old zanata.xml
            String suffix = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(
                    new Date());
            backup = new File(projectConfig.getParent(),
                    "zanata.xml." + suffix);
            FileUtils.moveFile(projectConfig, backup);
            consoleInteractor
                    .printfln(Confirmation, get("backup.old.project.config"), backup);

            clearValuesSetByConfigurableMojo();
        }
    }

    /**
     * ConfigurableMojo will set some options using existing config. Need to
     * clear it.
     */
    private void clearValuesSetByConfigurableMojo() {
        opts.setProj(null);
        opts.setProjectVersion(null);
        opts.setProjectType(null);
        opts.setProjectConfig(null);
        opts.setSrcDir(null);
        opts.setTransDir(null);
        opts.setIncludes(null);
        opts.setExcludes(null);
    }

    public InitOptions getOpts() {
        return opts;
    }

    public boolean hasOldConfig() {
        return backup != null;
    }

    public File getBackup() {
        return backup;
    }
}

