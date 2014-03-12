package org.zanata.util;

import org.junit.rules.ExternalResource;
import org.zanata.rest.SampleProjectResource;

import static org.zanata.util.SampleProjectClient.checkAndReleaseConnection;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CleanDatabaseRule extends ExternalResource {

    private final SampleProjectResource resource = SampleProjectClient.RESOURCE;

    @Override
    protected void before() throws Throwable {
        checkAndReleaseConnection(resource.deleteExceptEssentialData());
    }

    @Override
    protected void after() {
        checkAndReleaseConnection(resource.deleteExceptEssentialData());
    }
}
