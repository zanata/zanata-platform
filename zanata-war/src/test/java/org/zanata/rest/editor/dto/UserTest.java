package org.zanata.rest.editor.dto;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class UserTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {
        String json =
            "{\n" + "    \"username\" : \"_username\",\n" + "    \"email\" : \"test@test.com\",\n " +
                "\"name\" : \"testUser\",\n   \"gravatarHash\" : \"hash\"" +
                "\n}";

        User user= om.readValue(json, User.class);

        User expected = new User("_username", "test@test.com", "testUser", "hash");

        assertEquals(user, expected);
    }
}
