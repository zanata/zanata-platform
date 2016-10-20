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

import com.google.common.base.Strings;

import static org.zanata.client.commands.Messages.get;

public interface ConsoleInteractor {
    /**
     * Unless user answer yes or y, the program will terminate with exception.
     */
    void expectYes();

    /**
     * If answer is not valid, it will offer retry.
     */
    String expectAnswerWithRetry(AnswerValidator answersValidator);

    /**
     * printf in Information mode.
     */
    ConsoleInteractor printf(String printfFmt, Object... args);

    /**
     * prinf plus a new line in Information mode.
     */
    ConsoleInteractor printfln(String printfFmt, Object... args);

    ConsoleInteractor printf(DisplayMode mode, String printfFmt, Object... args);

    ConsoleInteractor printfln(DisplayMode mode, String printfFmt, Object... args);

    String expectAnyNotBlankAnswer();

    String expectAnyAnswer();

    void blankLine();

    interface AnswerValidator {

        static final AnswerValidator YES_NO = new AnswerValidator() {
            @Override
            public boolean isAnswerValid(String answer) {
                return answer.equalsIgnoreCase("y")
                        || answer.equalsIgnoreCase("n")
                        || answer.equalsIgnoreCase("yes")
                        || answer.equalsIgnoreCase("no");

            }

            @Override
            public String invalidErrorMessage(String answer) {
                return String.format(get("expected.and.actual.answer"), "y or n",
                        answer);
            }
        };
        static final AnswerValidator NOT_BLANK = new AnswerValidator() {
            @Override
            public boolean isAnswerValid(String answer) {
                return !Strings.isNullOrEmpty(answer);
            }

            @Override
            public String invalidErrorMessage(String answer) {
                return get("no.blank.answer");
            }
        };
        static final AnswerValidator ANY = new AnswerValidator() {
            @Override
            public boolean isAnswerValid(String answer) {
                return true;
            }

            @Override
            public String invalidErrorMessage(String answer) {
                throw new UnsupportedOperationException("any answer can not be invalid");
            }
        };

        boolean isAnswerValid(String answer);

        String invalidErrorMessage(String answer);
    }

    enum DisplayMode {
        Hint,
        Warning,
        Confirmation,
        Question,
        Information;

    }
}
