package org.zanata.rest.editor.dto;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.zanata.common.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {
        String json =
            "{\n" + "    \"localeId\" : \"de\",\n" + "    \"name\" : \"German\" \n}";
        Locale locale = om.readValue(json, Locale.class);

        Locale expected = new Locale(LocaleId.DE, "German");
        assertThat(locale).isEqualTo(expected);
    }
}
