/*
 * This work is licensed under a Creative Commons Attribution-ShareAlike 2.5
 * Generic License - See http://creativecommons.org/licenses/by-sa/2.5/
 */
package org.zanata.util

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 *
 * Provide a retry mechanism that allows a test to be executed
 * repeatedly until it has passed, or fails the given threshold. This
 * threshold is defined as the original execution, plus the number of
 * given retries, i.e<br></br>
 * RetryRule retryRule = new RetryRule(2);<br></br>
 * will execute a failing test 3 times before giving up.
 *
 * This file is an edited form of the work provided by
 * [Matthew Farwell](http://stackoverflow.com/users/1836/matthew-farwell) on
 * the Stack Exchange Network, specifically
 * [this submission](http://stackoverflow.com/a/8301639) to Stack Overflow.
 */
@Suppress("unused")
class RetryRule
/**
 * Constructor
 *
 * @param retries
 * maximum number of added attempts before failing a test
 */
(private val retries: Int) : TestRule {
    private var currentExecution: Int = 0

    /**
     * Return the human number of the current execution (1 for first try, etc)
     *
     * @return integer
     */
    fun currentTry(): Int {
        return currentExecution + 1
    }

    /**
     * Executes the TestRule repeatedly until the retries have been exceeded, or
     * the test passes
     *
     * @param base
     * TestRule base link
     * @param description
     * TestRule description link
     * @return TestRule statement
     * @see TestRule
     */
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var throwable: Throwable? = null
                currentExecution = 0
                while (currentExecution <= retries) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        throwable = t
                        log.warn(description.displayName + ": Execution "
                                + currentTry() + " failed", t)
                    }

                    currentExecution++
                }
                log.info(description.displayName + ": Failure threshold ("
                        + retries + ") exceeded")
                throw throwable!!
            }
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(RetryRule::class.java)
    }
}
