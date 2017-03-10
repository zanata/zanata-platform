package org.zanata.feature.clientserver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    GettextPluralSupportTest.class,
    ProjectMaintainerTest.class,
    PropertiesRoundTripTest.class
})
public class ClientServerSuite {
}
