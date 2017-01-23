/*
 * This work is licensed under a Creative Commons Attribution-ShareAlike 2.5
 * Generic License - See http://creativecommons.org/licenses/by-sa/2.5/
 */
package org.zanata.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 *
 *         Provide a retry mechanism that allows a test to be executed
 *         repeatedly until it has passed, or fails the given threshold. This
 *         threshold is defined as the original execution, plus the number of
 *         given retries, i.e<br/>
 *         RetryRule retryRule = new RetryRule(2);<br/>
 *         will execute a failing test 3 times before giving up.
 *
 *         This file is an edited form of the work provided by
 *         <a href="http://stackoverflow.com/users/1836/matthew-farwell">Matthew
 *         Farwell</a> on the Stack Exchange Network, specifically
 *         <a href="http://stackoverflow.com/a/8301639">this submission</a> to
 *         Stack Overflow.
 */
public class RetryRule implements TestRule {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RetryRule.class);
    private int retries;
    private int currentExecution;

    /**
     * Constructor
     *
     * @param retries
     *            maximum number of added attempts before failing a test
     */
    public RetryRule(int retries) {
        this.retries = retries;
    }

    /**
     * Return the human number of the current execution (1 for first try, etc)
     *
     * @return integer
     */
    public int currentTry() {
        return currentExecution + 1;
    }

    /**
     * Executes the TestRule repeatedly until the retries have been exceeded, or
     * the test passes
     *
     * @param base
     *            TestRule base link
     * @param description
     *            TestRule description link
     * @return TestRule statement
     * @see TestRule
     */
    public Statement apply(final Statement base,
            final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Throwable throwable = null;
                for (currentExecution =
                        0; currentExecution <= retries; currentExecution++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        throwable = t;
                        log.warn(description.getDisplayName() + ": Execution "
                                + (currentTry()) + " failed", t);
                    }
                }
                log.info(description.getDisplayName() + ": Failure threshold ("
                        + retries + ") exceeded");
                throw throwable;
            }
        };
    }
}
