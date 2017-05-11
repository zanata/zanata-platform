package org.zanata.feature.project;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CreateProjectTest.class,
    EditPermissionsTest.class,
    EditProjectAboutTest.class,
    EditProjectGeneralTest.class,
    EditProjectLanguagesTest.class,
    EditProjectValidationsTest.class,
    EditWebHooksTest.class,
    SetProjectVisibilityTest.class
})
public class ProjectTestSuite {
}
