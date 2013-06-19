package org.zanata.feature.security;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.util.ResetDatabaseRule;

import static org.zanata.util.ResetDatabaseRule.Config.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SecurityTest.class, LoginTest.class})
public class SecurityTestSuite
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule(WithAdmin, WithTranslator);
}
