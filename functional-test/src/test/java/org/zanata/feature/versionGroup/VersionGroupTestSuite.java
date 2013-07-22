package org.zanata.feature.versionGroup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
      VersionGroupTest.class,
      VersionGroupFullTest.class,
      VersionGroupBasicTest.class,
      VersionGroupIDValidationTest.class
})
public class VersionGroupTestSuite
{
}
