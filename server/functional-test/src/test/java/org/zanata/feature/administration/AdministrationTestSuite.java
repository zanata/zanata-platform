package org.zanata.feature.administration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AutoRoleAssignmentTest.class,
    EditHomePageTest.class,
    EditTranslationMemoryTest.class,
    ManageSearchTest.class,
    ManageUsersTest.class,
    ServerSettingsTest.class
})
public class AdministrationTestSuite {
}
