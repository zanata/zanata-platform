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
        String json =
            "{\n" + "    \"username\" : \"_username\",\n" + "    \"email\" : \"test@example.com\",\n " +
                "\"name\" : \"testUser\",\n   \"gravatarHash\" : \"hash\"" +
                "\n}";

        User user= om.readValue(json, User.class);

        User expected = new User("_username", "test@example.com", "testUser", "hash");

        assertThat(user).isEqualTo(expected);
    }
}
