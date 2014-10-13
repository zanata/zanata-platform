package org.zanata.rest.editor.dto;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TransUnitStatusTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {
        String json =
            "{\n" + "    \"id\" : \"100\",\n" + "    \"resId\" : \"rest id\",\n " +
                "\"status\" : \"NeedReview\""+
                "\n}";

        TransUnitStatus status = om.readValue(json, TransUnitStatus.class);

        TransUnitStatus expected = new TransUnitStatus(100L, "rest id",
            ContentState.NeedReview);

        assertEquals(status, expected);
    }
}
