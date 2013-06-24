package org.zanata.feature.glossary;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.util.ResetDatabaseRule;

import junit.framework.TestSuite;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({GlossaryTest.class, GlossaryPushTest.class})
public class GlossaryTestSuite
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule(ResetDatabaseRule.Config.NoResetAfter, ResetDatabaseRule.Config.WithLangFr);
}
