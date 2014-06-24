package org.zanata.util;

import org.junit.rules.ExternalResource;
import org.zanata.common.LocaleId;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import static org.zanata.util.SampleDataResourceClient.deleteExceptEssentialData;
import static org.zanata.util.SampleDataResourceClient.makeSampleLanguages;
import static org.zanata.util.SampleDataResourceClient.makeSampleUsers;
import static org.zanata.util.SampleDataResourceClient.userJoinsLanguageTeam;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class AddUsersRule extends ExternalResource {

    @Override
    protected void before() throws Exception {
        deleteExceptEssentialData();
        makeSampleUsers();
        makeSampleLanguages();
        userJoinsLanguageTeam("translator",
                Sets.newHashSet(LocaleId.FR, new LocaleId("hi"),
                        new LocaleId("pl")));
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
