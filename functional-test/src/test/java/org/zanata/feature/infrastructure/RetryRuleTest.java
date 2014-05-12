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

package org.zanata.feature.infrastructure;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.util.RetryRule;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 * href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(BasicAcceptanceTest.class)
public class RetryRuleTest extends ZanataTestCase {

    @Rule
    public RetryRule retryRule = new RetryRule(2);

    @Test
    public void retryPassAfterFail() {
        // Fail on the first execution, but pass on the second
        assertThat("Current try is greater than 1", retryRule.currentTry(),
                Matchers.greaterThan(1));
        // Can only pass on second execution
        assertThat("This is the second try", retryRule.currentTry(),
                Matchers.equalTo(2));
    }

    @Test
    public void passWillPass() {
        assertThat("A normal passing test will pass", true);
        assertThat("And pass on the first try", retryRule.currentTry(),
                Matchers.equalTo(1));
    }

    @Test(expected = AssertionError.class)
    public void retryFailsWhenAllTriesFail() throws Exception {
        // Fail the first execution
        if (retryRule.currentTry() == 1) {
            throw new Exception();
        }
        // Passes on the second execution, expect-fails on the third
        assertThat("The execution count is correct", retryRule.currentTry(),
                Matchers.equalTo(2));
        // Fails on the second execution
        throw new Exception();
    }
}
