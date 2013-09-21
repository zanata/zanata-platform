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
import org.jboss.aesh.console.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Command;
import org.jboss.aesh.console.CommandResult;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.CharacterType;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalCharacter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataConsole
{
   public static void main(String[] args)
   {
      List<TerminalCharacter> terminalChars =
         Lists.newArrayList(
               new TerminalCharacter('[', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('z', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('a', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('n', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('a', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('t', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('a', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter(']', Color.DEFAULT_BG, Color.BLUE_TEXT, CharacterType.BOLD ),
               new TerminalCharacter('$', Color.DEFAULT_BG, Color.YELLOW_TEXT, CharacterType.BOLD ),
               new TerminalCharacter(' ', Color.DEFAULT_BG, Color.DEFAULT_BG, CharacterType.NORMAL )
               );
      Prompt prompt = new Prompt(terminalChars);

      Settings settings = new SettingsBuilder().logging(true).create();
      AeshConsole aeshConsole = new AeshConsoleBuilder().settings(settings)
            .prompt(prompt)
            .commandRegistry(
                  new AeshCommandRegistryBuilder()
                        .command(ExitCommand.class)
                        .command(LsCommand.class)
                        .command(SampleCommand.class)
                        .command(HelpCommand.class)
                        .create())
            .create();

      aeshConsole.start();
   }

   @CommandDefinition(name = "exit", description = "exit the program")
   public static class ExitCommand implements Command
   {
      @Override
      public CommandResult execute(AeshConsole console, ControlOperator operator) throws IOException
      {
         console.stop();
         return CommandResult.SUCCESS;
      }
   }

   @CommandDefinition(name = "ls", description = "fooing")
   public static class LsCommand implements Command
   {
      @Option
      private String color;

      @Arguments
      private List<File> files;

      @Override
      public CommandResult execute(AeshConsole console, ControlOperator operator) throws IOException
      {
         if (files != null)
         {
            for (File f : files)
               console.out().print(f.toString());
         }
         return CommandResult.SUCCESS;
      }
   }

   @CommandDefinition(name = "sample", description = "a sample command")
   public static class SampleCommand implements Command
   {
      @Option
      private String option1;

      @Option
      private String option2;

      @Option
      private String option3;

      @Override
      public CommandResult execute(AeshConsole aeshConsole, ControlOperator operator) throws IOException
      {
         aeshConsole.out().println(option1 + ", " + option2 + ", " + option3);
         return CommandResult.SUCCESS;
      }
   }

   @CommandDefinition(name = "help", description = "Displays help for the console's commands")
   public static class HelpCommand implements Command
   {
      @Arguments(description = "Commands to show help for")
      private List<String> commandNames;

      @Override
      public CommandResult execute(AeshConsole aeshConsole, ControlOperator operator) throws IOException
      {
         if( commandNames == null )
         {
            commandNames = new ArrayList<String>();
            commandNames.addAll( aeshConsole.getCommandRegistry().getAllCommandNames() );
         }
         for( String command : commandNames )
         {
            String helpInfo = aeshConsole.getHelpInfo(command);
            aeshConsole.out().println(helpInfo);
         }
         return CommandResult.SUCCESS;
      }
   }

}
