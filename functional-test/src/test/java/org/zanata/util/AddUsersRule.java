package org.zanata.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class AddUsersRule extends ExternalResource {

    private SampleProjectProfile profile;

    @Override
    protected void before() {
        profile = new SampleProjectProfile();
        profile.deleteExceptEssentialData();
        profile.makeSampleUsers();
    }

    @Override
    protected void after() {
        profile.deleteExceptEssentialData();
    }
}
