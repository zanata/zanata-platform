package org.zanata.client.commands;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockConsoleInteractor implements ConsoleInteractor {
    private static final Logger log =
            LoggerFactory.getLogger(MockConsoleInteractor.class);
    private final Iterator<String> predefinedAnswers;
    private final ImmutableList.Builder<String> capturedPrompts = ImmutableList.builder();

    private MockConsoleInteractor(
            List<String> predefinedAnswers) {
        this.predefinedAnswers = ImmutableList.copyOf(predefinedAnswers)
                .iterator();
    }

    public static ConsoleInteractor predefineAnswers(String... answers) {
        return new MockConsoleInteractor(Lists.newArrayList(answers));
    }

    @Override
    public void expectYes() {
        log.debug("expecting yes");
        simulateUserInputUsingNextPredefinedAnswer();
    }

    @Override
    public String expectAnswerWithRetry(
            AnswerValidator answersValidator) {
        log.debug("** expected answers: {}", answersValidator);
        return simulateUserInputUsingNextPredefinedAnswer();
    }

    @Override
    public ConsoleInteractor printf(String printfFmt, Object... args) {
        log.debug(String.format(printfFmt, args));
        capturedPrompts.add(String.format(printfFmt, args));
        return this;
    }

    @Override
    public ConsoleInteractor printfln(String printfFmt, Object... args) {
        return printf(printfFmt, args);
    }

    @Override
    public ConsoleInteractor printf(DisplayMode mode, String printfFmt,
            Object... args) {
        return printf(printfFmt, args);
    }

    @Override
    public ConsoleInteractor printfln(DisplayMode mode, String printfFmt,
            Object... args) {
        return printfln(printfFmt, args);
    }

    @Override
    public String expectAnyNotBlankAnswer() {
        return simulateUserInputUsingNextPredefinedAnswer();
    }

    @Override
    public String expectAnyAnswer() {
        return simulateUserInputUsingNextPredefinedAnswer();
    }

    @Override
    public void blankLine() {
        printfln("");
    }

    public static List<String> getCapturedPrompts(
            ConsoleInteractor consoleInteractor) {
        Preconditions
                .checkArgument(
                        consoleInteractor instanceof MockConsoleInteractor);
        return MockConsoleInteractor.class.cast(consoleInteractor).capturedPrompts
                .build();
    }

    private String simulateUserInputUsingNextPredefinedAnswer() {
        Preconditions.checkState(predefinedAnswers.hasNext(),
                "You run out of predefined answers! Check your test setup.");
        String userInput = predefinedAnswers.next();
        log.debug("** simulating user input:{}", userInput);
        return userInput;
    }
}
