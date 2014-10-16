package org.zanata.rest.editor.dto;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;
import org.zanata.rest.dto.resource.TextFlowTarget;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TransUnitTest {

    @Test(expectedExceptions = ClassCastException.class)
    public void testPutInvalidSource() {
        TransUnit tu = new TransUnit();
        tu.put(TransUnit.SOURCE, "String");
    }

    @Test
    public void testPutValidSource() {
        TransUnit tu = new TransUnit();
        tu.put(TransUnit.SOURCE, new EditorTextFlow());
        assertEquals(tu.size(), 1);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testPutInvalidTarget() {
        TransUnit tu = new TransUnit();
        tu.put("fr", "String");
    }

    @Test
    public void testPutValidTarget() {
        TransUnit tu = new TransUnit();
        tu.put("fr", new TextFlowTarget());
        assertEquals(tu.size(), 1);
    }
}
