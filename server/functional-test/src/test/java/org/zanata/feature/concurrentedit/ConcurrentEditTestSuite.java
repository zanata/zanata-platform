package org.zanata.feature.concurrentedit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConcurrentAccessTest.class,
        ConcurrentEditTest.class
})
public class ConcurrentEditTestSuite {
}
