package org.zanata.feature.security;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ SecurityTest.class, LoginTest.class,
        SecurityFullTest.class })
public class SecurityTestSuite {
}
