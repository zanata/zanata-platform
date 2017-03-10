package org.zanata.feature.glossary;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    GlossaryAdminTest.class,
    GlossaryPushTest.class,
    InvalidGlossaryPushTest.class
})
public class GlossaryTestSuite {
}
