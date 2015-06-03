package org.zanata.rest.editor.dto;

import org.junit.Test;
import org.zanata.rest.dto.resource.TextFlowTarget;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TransUnitTest {

    @Test(expected = ClassCastException.class)
    public void testPutInvalidSource() {
        TransUnit tu = new TransUnit();
        tu.put(TransUnit.SOURCE, "String");
    }

    @Test
    public void testPutValidSource() {
        TransUnit tu = new TransUnit();
        tu.put(TransUnit.SOURCE, new EditorTextFlow());
        assertThat(tu.size()).isEqualTo(1);
    }

    @Test(expected = ClassCastException.class)
    public void testPutInvalidTarget() {
        TransUnit tu = new TransUnit();
        tu.put("fr", "String");
    }

    @Test
    public void testPutValidTarget() {
        TransUnit tu = new TransUnit();
        tu.put("fr", new TextFlowTarget());
        assertThat(tu.size()).isEqualTo(1);
    }
}
