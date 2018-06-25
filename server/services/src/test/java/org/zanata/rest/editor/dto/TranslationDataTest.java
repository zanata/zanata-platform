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
                "{\n" + "    \"id\" : \"10\",\n" +
                        "    \"revision\" : \"10000\",\n" +
                        "\"plural\" : false,\n" +
                        "\"revisionComment\" : \"comment\",\n" +
                        "\"status\" : \"NeedReview\",\n" +
                        "\"lastModifiedBy\" : \"aeng\",\n" +
                        "\"lastModifiedDate\" :" +
                        " \"Mon Jun 25 01:01:01 UTC 2018\" " +
                        "\n}";

        TranslationData translationData = om.readValue(json, TranslationData.class);

        TranslationData expected = new TranslationData();
        expected.setId(10);
        expected.setRevision(10000);
        expected.setPlural(false);
        expected.setStatus(ContentState.NeedReview);
        expected.setRevisionComment("comment");
        expected.setLastModifiedBy("aeng");
        expected.setLastModifiedDate("Mon Jun 25 01:01:01 UTC 2018");

        assertThat(translationData).isEqualTo(expected);
    }

}
