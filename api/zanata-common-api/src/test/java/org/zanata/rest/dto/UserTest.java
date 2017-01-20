package org.zanata.rest.dto;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.zanata.common.LocaleId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class UserTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {
        String json =
            "{\"username\":\"_username\",\"email\":\"test@example.com\",\"name\":\"testUser\",\"imageUrl\":\"url\",\"languageTeams\":[\"zh-Hans\",\n" +
                "\"fr\"],\"loggedIn\":true}";
        User user = om.readValue(json, User.class);
        ArrayList<LocaleId> list = new ArrayList<LocaleId>();
        list.add(new LocaleId("zh-Hans"));
        list.add(LocaleId.FR);

        User expected = new User("_username", "test@example.com", "testUser",
                "url", list, null);

        assertThat(user, equalTo(expected));
    }
}
