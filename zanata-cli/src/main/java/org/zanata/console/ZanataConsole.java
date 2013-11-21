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
package org.zanata.console;

import com.google.common.collect.Lists;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.internal.ProcessedCommand;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.CharacterType;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalCharacter;
import org.jboss.aesh.terminal.TerminalColor;
import org.zanata.client.commands.stats.GetStatisticsCommand;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;
import org.zanata.console.command.GetStatisticsConsoleCommand;
import org.zanata.console.util.AeshCommandGenerator;
import org.zanata.console.util.Args4jCommandGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataConsole {
    public static void main(String[] args) throws Exception {
        List<TerminalCharacter> terminalChars =
                Lists.newArrayList(
                    new TerminalCharacter('z', new TerminalColor(Color.BLUE, Color.DEFAULT), CharacterType.BOLD),
                    new TerminalCharacter('a', new TerminalColor(Color.BLUE, Color.DEFAULT), CharacterType.BOLD),
                    new TerminalCharacter('n', new TerminalColor(Color.BLUE, Color.DEFAULT), CharacterType.BOLD),
                    new TerminalCharacter('a', new TerminalColor(Color.BLUE, Color.DEFAULT), CharacterType.BOLD),
                    new TerminalCharacter('t', new TerminalColor(Color.BLUE, Color.DEFAULT), CharacterType.BOLD),
                    new TerminalCharacter('a', new TerminalColor(Color.BLUE, Color.DEFAULT), CharacterType.BOLD),
                    new TerminalCharacter('>', new TerminalColor(Color.BLUE, Color.YELLOW), CharacterType.BOLD),
                    new TerminalCharacter(' ', new TerminalColor(Color.DEFAULT, Color.DEFAULT)));
        Prompt prompt = new Prompt(terminalChars);

        Settings settings = new SettingsBuilder().logging(true).create();
        
        ProcessedCommand statsProcessedCommand = Args4jCommandGenerator
            .generateCommand(
                "stats",
                "",
                GetStatisticsOptionsImpl.class);
        Class<?> commandClass = AeshCommandGenerator.generateCommandClass(statsProcessedCommand);
        Command statsCmd = (Command)commandClass.getConstructor(Class.class, Class.class).newInstance(
            GetStatisticsCommand.class, GetStatisticsOptionsImpl.class);
        
        AeshConsole aeshConsole =
                new AeshConsoleBuilder()
                        .settings(settings)
                        .prompt(prompt)
                        //.commandInvocationProvider()
                        .commandRegistry(
                            new AeshCommandRegistryBuilder()
                                .command(statsProcessedCommand,
                                    statsCmd)
                                .command(ExitCommand.class)
                                .command(LsCommand.class)
                                .command(HelpCommand.class).create())
                        .create();

        aeshConsole.start();
    }

    @CommandDefinition(name = "exit", description = "exit the program")
    public static class ExitCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation)
            throws IOException {
            commandInvocation.stop();
            return CommandResult.SUCCESS;
        }
    }

    @CommandDefinition(name = "ls", description = "fooing")
    public static class LsCommand implements Command {
        @Option
        private String color;

        @Arguments
        private List<File> files;

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
            if (files != null) {
                for (File f : files)
                    commandInvocation.getShell().out().println(f.toString());
            }
            return CommandResult.SUCCESS;
        }
    }

    @CommandDefinition(name = "help",
            description = "Displays help for the console's commands")
    public static class HelpCommand implements Command {
        @Arguments(description = "Commands to show help for")
        private List<String> commandNames;

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
            if (commandNames == null) {
                commandNames = new ArrayList<String>();
                commandNames.addAll(commandInvocation.getCommandRegistry()
                        .getAllCommandNames());
            }
            for (String command : commandNames) {
                String helpInfo = commandInvocation.getHelpInfo(command);
                commandInvocation.getShell().out().println(helpInfo);
            }
            return CommandResult.SUCCESS;
        }
    }

}
