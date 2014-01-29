package org.zanata.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.zanata.common.LocaleId;
import org.zanata.rest.SampleProjectResource;

import com.google.common.collect.Sets;

import static org.zanata.util.SampleProjectClient.checkAndReleaseConnection;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class AddUsersRule extends ExternalResource {

    private final SampleProjectResource resource = SampleProjectClient.RESOURCE;

    @Override
    protected void before() {
        checkAndReleaseConnection(resource.deleteExceptEssentialData());
        checkAndReleaseConnection(resource.makeSampleUsers());
        checkAndReleaseConnection(resource.userJoinsLanguageTeams("translator",
                Sets.newHashSet(LocaleId.FR, new LocaleId("hi"),
                        new LocaleId("pl"))));
    }

    @Override
    protected void after() {
        checkAndReleaseConnection(resource.deleteExceptEssentialData());
    }
}
