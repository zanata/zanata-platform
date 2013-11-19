/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.client.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.config.CommandHook;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.rest.client.ZanataProxyFactory;

/**
 * Base class for commands which supports configuration by the user's zanata.ini
 * and by a project's zanata.xml
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public abstract class ConfigurableProjectCommand<O extends ConfigurableProjectOptions>
        extends ConfigurableCommand<O> {

    private static final Logger log = LoggerFactory
            .getLogger(PullCommand.class);

    protected static final String PROJECT_TYPE_UTF8_PROPERTIES =
            "utf8properties";
    protected static final String PROJECT_TYPE_PROPERTIES = "properties";
    protected static final String PROJECT_TYPE_GETTEXT = "gettext";
    protected static final String PROJECT_TYPE_PUBLICAN = "podir";
    protected static final String PROJECT_TYPE_XLIFF = "xliff";
    protected static final String PROJECT_TYPE_XML = "xml";
    protected static final String PROJECT_TYPE_FILE = "file";

    public ConfigurableProjectCommand(O opts, ZanataProxyFactory factory) {
        super(opts, factory);
        if (opts.getProj() == null)
            throw new ConfigException("Project must be specified");
        if (opts.getProjectVersion() == null)
            throw new ConfigException("Project version must be specified");
        if (getProjectType() == null)
            throw new ConfigException("Project type must be specified");
    }

    public ConfigurableProjectCommand(O opts) {
        this(opts, null);
    }

    protected String getProjectType() {
        return this.getOpts().getProjectType();
    }

    @Override
    public void run() throws Exception {
        runBeforeActions();
        performWork();
        runAfterActions();
    };

    protected abstract void performWork() throws Exception;

    /**
     * @throws Exception
     *             if any of the commands fail
     */
    private void runBeforeActions() throws Exception {
        for (CommandHook hook : getOpts().getCommandHooks()) {
            if (isForThisCommand(hook)) {
                runSystemCommands(hook.getBefores());
            }
        }
    }

    /**
     * @throws Exception
     *             if any of the commands fail.
     */
    private void runAfterActions() throws Exception {
        for (CommandHook hook : getOpts().getCommandHooks()) {
            if (isForThisCommand(hook)) {
                runSystemCommands(hook.getAfters());
            }
        }
    }

    /**
     * @throws Exception
     *             if any of the commands fail.
     */
    private void runSystemCommands(List<String> commands) throws Exception {
        for (String command : commands) {
            log.info("[Running command]$ " + command);
            try {
                Process proc = Runtime.getRuntime().exec(command);
                proc.waitFor();
                if (proc.exitValue() != 0) {
                    throw new Exception(
                            "Command returned non-zero exit value: "
                                    + proc.exitValue());
                }
                log.info("    Completed with exit value: " + proc.exitValue());
            } catch (java.io.IOException e) {
                throw new Exception("Failed to run command. " + e.getMessage(),
                        e);
            } catch (InterruptedException e) {
                throw new Exception("Interrupted while running command. "
                        + e.getMessage(), e);
            }
        }
    }

    private boolean isForThisCommand(CommandHook hook) {
        return this.getName().equals(hook.getCommand());
    }
}
