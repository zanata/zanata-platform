package org.zanata.rest.dto;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.zanata.common.ContentState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TransUnitStatusTest {

    ObjectMapper om = new ObjectMapper();

    String jsonInput =
            "{\n" + "    \"id\" : \"100\",\n" + "    \"resId\" : \"rest id\",\n "
                + "\"status\" : \"NeedReview\", \"transSourceType\" : \"MT\"" + "\n}";

    String jsonOutput =
            "{\"id\":100,\"resId\":\"rest id\",\"status\":\"NeedReview\",\"transSourceType\":\"MT\"}";

    TransUnitStatus status = new TransUnitStatus(100L, "rest id",
            ContentState.NeedReview, TranslationSourceType.MACHINE_TRANS);

    @Test
    public void testJsonReading() throws IOException {
        TransUnitStatus actual = om.readValue(jsonInput, TransUnitStatus.class);
        assertThat(actual, equalTo(status));
    }

    @Test
    public void testJsonWriting() throws IOException {
        String actual = om.writeValueAsString(status);
        assertThat(actual, equalTo(jsonOutput));

    }
}
