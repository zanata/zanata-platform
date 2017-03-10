package org.zanata.feature.projectversion;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CreateProjectVersionTest.class,
    EditVersionLanguagesTest.class,
    EditVersionSlugTest.class,
    EditVersionValidationsTest.class,
    VersionFilteringTest.class
})
public class ProjectVersionTestSuite {
}
