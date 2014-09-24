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

import java.io.Console;
import java.io.IOException;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.config.CommandHook;
import org.zanata.rest.client.ZanataProxyFactory;

/**
 * Base class for commands which supports configuration by the user's zanata.ini
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public abstract class ConfigurableCommand<O extends ConfigurableOptions>
        implements ZanataCommand {
    private final O opts;
    private ZanataProxyFactory requestFactory;
    private boolean deprecated;
    private String deprecationMessage;

    private static final Logger log = LoggerFactory
            .getLogger(ConfigurableCommand.class);

    public ConfigurableCommand(O opts, ZanataProxyFactory factory) {
        this.opts = opts;
        if (factory != null)
            this.requestFactory = factory;
        else
            this.requestFactory = OptionsUtil.createRequestFactory(opts);
    }

    public ConfigurableCommand(O opts) {
        this(opts, null);
    }

    // see ConsoleInteractorImpl
    @Deprecated
    protected static void expectYes(Console console) throws IOException {
        String line = console.readLine();
        if (line == null) {
            throw new IOException("console stream closed");
        }
        if (!line.toLowerCase().equals("y")
                && !line.toLowerCase().equals("yes")) {
            throw new RuntimeException("operation aborted by user");
        }
    }

    public O getOpts() {
        return opts;
    }

    public ZanataProxyFactory getRequestFactory() {
        return requestFactory;
    }

    @Override
    public boolean isDeprecated() {
        return deprecated;
    }

    @Override
    public String getDeprecationMessage() {
        return this.deprecationMessage;
    }

    public void deprecate(String deprecationMessage) {
        this.deprecated = true;
        this.deprecationMessage = deprecationMessage;
    }

    @Override
    public String getName() {
        return opts.getCommandName();
    }

    @Override
    public void runWithActions() throws Exception {
        runBeforeActions();
        run();
        runAfterActions();
    }

    /**
     * Runs the specific command, not including before- or after- actions.
     *
     * @throws Exception
     */
    protected abstract void run() throws Exception;

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
                DefaultExecutor executor = new DefaultExecutor();
                executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
                CommandLine cmdLine = CommandLine.parse(command);
                int exitValue = executor.execute(cmdLine);

                if (exitValue != 0) {
                    throw new Exception(
                            "Command returned non-zero exit value: "
                                    + exitValue);
                }
                log.info("    Completed with exit value: " + exitValue);
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
