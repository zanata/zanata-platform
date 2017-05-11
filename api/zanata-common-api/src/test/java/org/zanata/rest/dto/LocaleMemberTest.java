package org.zanata.rest.dto;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleMemberTest {

    @Test
    public void testConstructor() {
        String username = "user1";
        LocaleMember member = new LocaleMember(username, true, true, true);
        assertThat(member.getUsername(), equalTo(username));
        assertThat(member.getCoordinator(), equalTo(true));
        assertThat(member.getReviewer(), equalTo(true));
        assertThat(member.getTranslator(), equalTo(true));
    }

    @Test
    public void testEqualsAndHashCode() {
        LocaleMember member = new LocaleMember("user1", true, true, true);
        LocaleMember member2 = new LocaleMember("user1", true, true, true);
        assertThat(member.hashCode(), equalTo(member2.hashCode()));
        assertThat(member.equals(member2), equalTo(true));

        // different username
        member2 = new LocaleMember("user2", true, true, true);
        assertThat(member.hashCode(), not(equalTo(member2.hashCode())));
        assertThat(member.equals(member2), equalTo(false));

        // different coordinator
        member2 = new LocaleMember("user1", false, true, true);
        assertThat(member.hashCode(), not(equalTo(member2.hashCode())));
        assertThat(member.equals(member2), equalTo(false));

        // different reviewer
        member2 = new LocaleMember("user1", true, false, true);
        assertThat(member.hashCode(), not(equalTo(member2.hashCode())));
        assertThat(member.equals(member2), equalTo(false));

        // different translator
        member2 = new LocaleMember("user1", true, true, false);
        assertThat(member.hashCode(), not(equalTo(member2.hashCode())));
        assertThat(member.equals(member2), equalTo(false));
    }
}
