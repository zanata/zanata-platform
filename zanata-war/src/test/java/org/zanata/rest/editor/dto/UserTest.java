package org.zanata.rest.editor.dto;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class UserTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {
        String json = "{\"username\":\"_username\",\"email\":\"test@example.com\",\"name\":\"testUser\",\"gravatarHash\":\"hash\",\"imageUrl\":\"url\",\"languageTeams\":\"English\",\"loggedIn\":true}";
        User user= om.readValue(json, User.class);

        User expected = new User("_username", "test@example.com", "testUser", "hash", "url", "English", true);
        assertThat(user).isEqualTo(expected);
    }
}
