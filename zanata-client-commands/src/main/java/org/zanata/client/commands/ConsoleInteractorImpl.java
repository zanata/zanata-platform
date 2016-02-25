/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands;

import java.io.Console;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.zanata.client.commands.StringUtil.indent;
import static org.zanata.client.commands.Messages.get;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ConsoleInteractorImpl implements ConsoleInteractor {
    private static final String CONSOLE_CLOSE_ERROR = "console stream closed";
    private final Console console;
    private final PrintStream out;

    public ConsoleInteractorImpl(BasicOptions options) {
        console = System.console();
        if (console == null && options.isInteractiveMode()) {
            throw new RuntimeException("Console not available: please run from a console, or use batch mode.");
        }
        out = AnsiConsole.out();
    }

    @Override
    public void expectYes() {
        String line = readLine();
        if (!line.toLowerCase().equals("y")
                && !line.toLowerCase().equals("yes")) {
            throw new RuntimeException("operation aborted by user");
        }
    }

    private String readLine() {
        String line = console.readLine();
        Preconditions.checkNotNull(line, CONSOLE_CLOSE_ERROR);
        return line;
    }

    @Override
    public String expectAnswerWithRetry(AnswerValidator answersValidator) {
        String line = readLine();
        if (answersValidator.isAnswerValid(line)) {
            return line;
        } else {
            printfln(DisplayMode.Warning, answersValidator.invalidErrorMessage(line));
            printf(get("re-enter.prompt"));
            return expectAnswerWithRetry(answersValidator);
        }
    }

    @Override
    public ConsoleInteractor printf(String printfFmt, Object... args) {
        return printf(DisplayMode.Information, printfFmt, args);
    }

    @Override
    public ConsoleInteractor printfln(String printfFmt, Object... args) {
        return printfln(DisplayMode.Information, printfFmt, args);
    }

    @Override
    public ConsoleInteractor printf(DisplayMode mode, String printfFmt, Object... args) {
        out.print(ModeDecorator.of(mode).format(printfFmt, args));
        out.flush();
        return this;
    }

    @Override
    public ConsoleInteractor printfln(DisplayMode mode, String printfFmt,
            Object... args) {
        out.println(ModeDecorator.of(mode).format(printfFmt, args));
        return this;
    }

    @Override
    public String expectAnyNotBlankAnswer() {
        return expectAnswerWithRetry(AnswerValidator.NOT_BLANK);
    }

    @Override
    public String expectAnyAnswer() {
        return expectAnswerWithRetry(AnswerValidator.ANY);
    }

    @Override
    public void blankLine() {
        out.println();
    }

    private static class ModeDecorator {
        private static final String NEW_LINE =
                System.getProperty("line.separator");
        private static final boolean ansiEnabled = Boolean.valueOf(System.getProperty("colorOutput", "true"));
        private final DisplayMode mode;

        private ModeDecorator(DisplayMode mode) {
            this.mode = mode;
        }

        static ModeDecorator of(DisplayMode mode) {
            return new ModeDecorator(mode);
        }
        String format(String format, Object... args) {
            Ansi ansi = Ansi.ansi();
            Ansi.setEnabled(ansiEnabled);
            switch (mode) {
                case Hint:
                    ansi.a(indent(4)).fg(Ansi.Color.CYAN);
                    break;
                case Warning:
                    ansi.a("[!] ").fg(Ansi.Color.RED);
                    break;
                case Confirmation:
                    ansi.a("[>] ").fgBright(Ansi.Color.DEFAULT);
                    break;
                case Question:
                    ansi.a("[?] ").fg(Ansi.Color.YELLOW);
                    break;
                case Information:
                    ansi.a(indent(4)).fg(Ansi.Color.DEFAULT);
                    break;
            }
            ansi = ansi.format(format, args);
            if (mode == DisplayMode.Confirmation) {
                ansi = ansi.a(NEW_LINE);
            }
            if (mode == DisplayMode.Question) {
                // append an extra space at the end of the question
                ansi = ansi.a(" ");
            }
            return ansi.reset().toString();
        }
    }

    public static class AnswerValidatorImpl implements AnswerValidator {

        private List<String> expectedAnswers = Collections.emptyList();

        private AnswerValidatorImpl(List<String> expectedAnswers) {
            this.expectedAnswers = ImmutableList.copyOf(expectedAnswers);
        }

        public static AnswerValidator expect(String... answers) {
            return new AnswerValidatorImpl(Lists.newArrayList(answers));
        }
        public static AnswerValidator expect(List<String> answers) {
            return new AnswerValidatorImpl(answers);
        }

        @Override
        public boolean isAnswerValid(String answer) {
            return expectedAnswers.contains(answer);
        }

        @Override
        public String invalidErrorMessage(String answer) {
            String expected =
                    Iterables.toString(expectedAnswers);
            if (expected.length() < 200) {
                return String.format(get("expected.and.actual.answer"),
                        expectedAnswers, answer);
            } else {
                return String.format(get("invalid.answer"), answer);
            }
        }

        @Override
        public String toString() {
            return expectedAnswers.toString();
        }
    }
}

