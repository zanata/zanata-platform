package org.zanata.rest.editor.dto;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.zanata.common.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class EditorTextFlowTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonOutput() throws IOException {

        String json =
            "{\n" + "    \"id\" : \"100\",\n" + "    \"revision\" : 1,\n"
                + "    \"lang\" : \"de\",\n"
                + "    \"contents\" : [\"plural1\", \"plural2\"],\n"
                + "\"wordCount\" : 10 }";


        EditorTextFlow tf = om.readValue(json, EditorTextFlow.class);


        EditorTextFlow expected = new EditorTextFlow("100", LocaleId.DE);
        expected.setContents("plural1", "plural2");
        expected.setRevision(1);
        expected.setWordCount(10);

        assertEquals(tf, expected);
    }

}
