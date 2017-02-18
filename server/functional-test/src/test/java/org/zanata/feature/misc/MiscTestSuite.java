package org.zanata.feature.misc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ContactAdminTest.class,
    FlakyTest.class,
    ObsoleteTextTest.class,
    PageNotFoundTest.class,
    RateLimitRestAndUITest.class

})
public class MiscTestSuite {
}
