package org.zanata.feature.glossary;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.util.ResetDatabaseRule;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Suite.class)
// @formatter:off
@Suite.SuiteClasses({GlossaryTest.class,
      GlossaryPushTest.class,
      InvalidGlossaryPushTest.class,
      GlossaryPushCSVTest.class,
      UnauthorizedGlossaryDeleteTest.class,
      UnauthorizedGlossaryPushTest.class,
      GlossaryDeleteTest.class
})
// @formatter:on
public class GlossaryTestSuite
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule(ResetDatabaseRule.Config.NoResetAfter, ResetDatabaseRule.Config.WithData);
}
