package org.zanata.rest.editor.dto;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.zanata.common.ContentState;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationDataTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {
        String json =
            "{\n" + "    \"id\" : \"10\",\n" + "    \"revision\" : \"10000\",\n" +
                "\"plural\" : false,\n" + "     \"status\" : \"NeedReview\" " +
                "\n}";

        TranslationData translationData = om.readValue(json, TranslationData.class);

        TranslationData expected = new TranslationData();
        expected.setId(10);
        expected.setRevision(10000);
        expected.setPlural(false);
        expected.setStatus(ContentState.NeedReview);

        assertThat(translationData).isEqualTo(expected);
    }

}
