package org.zanata.util;

import org.junit.rules.ExternalResource;
import com.google.common.base.Throwables;

import static org.zanata.util.SampleDataResourceClient.deleteExceptEssentialData;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CleanDatabaseRule extends ExternalResource {


    @Override
    protected void before() throws Throwable {
        deleteExceptEssentialData();
    }

    @Override
    protected void after() {
        try {
            deleteExceptEssentialData();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
