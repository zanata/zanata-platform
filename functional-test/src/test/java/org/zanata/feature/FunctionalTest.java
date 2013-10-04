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
package org.zanata.feature;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.zanata.page.WebDriverFactory;
import org.zanata.page.utility.HomePage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.AbstractWebWorkFlow;

import java.util.Date;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
@Category(DetailedTest.class)
public class FunctionalTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

    @Rule
    public TestName name = new TestName();

    @Before
    public void setupTestFunction() {
        WebDriverFactory.INSTANCE.getDriver();
        updateTestId();
        String date = new Date().toString();
        log.info("[TEST] {}:{}:{}", getClass().getName(), name.getMethodName(), date);
    }

    private void updateTestId() {
        String testId = getClass().getName();
        testId = testId.substring(testId.lastIndexOf(".")+1)
            .concat(":").concat(name.getMethodName());
        WebDriverFactory.INSTANCE.updateListenerTestName(testId);
    }
}
