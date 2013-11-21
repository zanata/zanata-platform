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
package org.zanata.console.command;

import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConfigurableOptions;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ConsoleCommand implements Command {

    private Class<? extends ConfigurableCommand> commandClass;
    private Class<? extends ConfigurableOptions> optionsClass;

    public ConsoleCommand(
        Class<? extends ConfigurableCommand> commandClass,
        Class<? extends ConfigurableOptions> optionsClass) {
        this.commandClass = commandClass;
        this.optionsClass = optionsClass;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation)
        throws IOException {

        ConfigurableCommand command = null;
        for(Constructor cons : commandClass.getConstructors()) {
            if(cons.getParameterTypes().length == 1 &&
                ConfigurableOptions.class.isAssignableFrom( cons.getParameterTypes()[0] )) {
                try {
                    command =
                        (ConfigurableCommand)cons.newInstance( buildOptions(this, optionsClass) );
                }
                catch (InstantiationException e) {
                    throw new IOException(e.getCause().getMessage());
                }
                catch (IllegalAccessException e) {
                    throw new IOException(e.getMessage());
                }
                catch (InvocationTargetException e) {
                    throw new IOException(e.getCause().getMessage());
                }
                catch (NoSuchMethodException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }

        try {
            command.run();
        }
        catch (Exception e) {
            e.printStackTrace();
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

    private <T extends ConfigurableOptions> T buildOptions(ConsoleCommand consoleCommand,
        Class<T> optionsClass)
        throws IllegalAccessException, InstantiationException,
        NoSuchMethodException, InvocationTargetException {

        ConfigurableOptions opts = (ConfigurableOptions)optionsClass.newInstance();

        for( Field f : consoleCommand.getClass().getDeclaredFields() ) {
            if( !f.getName().equals("commandClass") && !f.getName().equals("optionsClass") ) {
                opts.getClass().getMethod("set" + f.getName().substring(0,1).toUpperCase() + f.getName().substring(1), f.getType())
                    .invoke(opts, f.get(consoleCommand));
            }
        }
        return (T)opts;
    }
}
