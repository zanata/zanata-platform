package org.zanata.feature.administration;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.util.ResetDatabaseRule;

import static org.zanata.util.ResetDatabaseRule.Config.*;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ManageUsersTest.class, ManageUsersDetailedTest.class})
public class AdministrationTestSuite
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();
}
