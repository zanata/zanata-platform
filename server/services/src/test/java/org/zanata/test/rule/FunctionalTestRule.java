/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.test.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit test rule which uses functional concepts to be able to intercept
 * method calls and re-order invocations. This rule can be used as both a
 * class level and method level rule simultaneously.
 *
 * It also allows to create reentrant rules (rules that understand if they
 * have already been invoked at the class level).
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class FunctionalTestRule implements TestRule {

    private static final RuleEffect NoOp = () -> {};

    protected RuleEffect beforeClass = NoOp;
    protected RuleEffect afterClass = NoOp;
    protected RuleEffect before = NoOp;
    protected RuleEffect after = NoOp;

    // For reentrant rules
    protected int counter = 0;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean isClassLevel = description.getMethodName() == null;
                if(isClassLevel) {
                    beforeClass.run();
                } else {
                    before.run();
                }
                try {
                    base.evaluate();
                } finally {
                    if (isClassLevel) {
                        afterClass.run();
                    } else {
                        after.run();
                    }
                }
            }
        };
    }

    protected void beforeClass(RuleEffect doBeforeClass) {
        beforeClass = beforeClass.andThen(doBeforeClass);
    }

    protected void afterClass(RuleEffect doAfterClass) {
        afterClass = afterClass.andThen(doAfterClass);
    }

    protected void before(RuleEffect doBefore) {
        before = before.andThen(doBefore);
    }

    protected void after(RuleEffect doAfter) {
        after = after.andThen(doAfter);
    }

    public static <T extends FunctionalTestRule> T reentrant(T from) {
        RuleEffect prevBeforeClass = from.beforeClass;
        from.beforeClass = () -> {
            if (from.counter++ == 0) {
                prevBeforeClass.run();
            }
        };

        RuleEffect prevAfterClass = from.afterClass;
        from.afterClass = () -> {
            if(--from.counter == 0) {
                prevAfterClass.run();
            }
        };

        return from;
    }

    @FunctionalInterface
    public interface RuleEffect extends Runnable {

        default RuleEffect andThen(Runnable runnable) {
            return () -> {
                this.run();
                runnable.run();
            };
        }
    }
}
