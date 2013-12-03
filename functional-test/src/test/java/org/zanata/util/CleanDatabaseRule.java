package org.zanata.util;

import org.junit.rules.ExternalResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CleanDatabaseRule extends ExternalResource {

    private SampleProjectProfile profile;

    @Override
    protected void before() throws Throwable {
        super.before();
        profile = new SampleProjectProfile();
        profile.deleteExceptEssentialData();
    }

    @Override
    protected void after() {
        super.after();
        profile.deleteExceptEssentialData();
    }
}
