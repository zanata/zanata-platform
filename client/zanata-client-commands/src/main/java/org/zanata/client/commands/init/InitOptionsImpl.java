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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableProjectOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class InitOptionsImpl extends ConfigurableProjectOptionsImpl implements
        InitOptions {
    private static final Logger log = LoggerFactory
            .getLogger(InitOptionsImpl.class);

    @Override
    public ZanataCommand initCommand() {
        return new InitCommand(this);
    }

    @Override
    public String getCommandName() {
        return "init";
    }

    @Override
    public String getCommandDescription() {
        return "Initialize Zanata project configuration";
    }

    @Override
    public void setInteractiveMode(boolean interactiveMode) {
        if (!interactiveMode) {
            log.warn(getCommandName()
                    + " command can only be run with interactive mode. Interactive mode option ignored.");
        }
        super.setInteractiveMode(true);
    }
}
