package org.zanata.adapter.asciidoc;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AsciidocUtilsTest {

    @Test
    public void getResId() {
        String msg = "msg";
        assertThat(AsciidocUtils.getResId(msg)).isNotBlank();

        msg = null;
        assertThat(AsciidocUtils.getResId(msg)).isNotBlank();
    }

    @Test
    public void getResIdList() {
        List<String> msgs = Arrays.asList("test", "test1");
        assertThat(AsciidocUtils.getResId(msgs)).isNotBlank();

        msgs = null;
        assertThat(AsciidocUtils.getResId(msgs)).isNotBlank();
    }

    @Test
    public void isAdmonition() {
        Map<String, Object> attributes = new HashMap();
        assertThat(AsciidocUtils.isAdmonition(attributes)).isFalse();

        attributes.put("style", "not admonition");
        assertThat(AsciidocUtils.isAdmonition(attributes)).isFalse();

        attributes.put("style", "NOTE");
        assertThat(AsciidocUtils.isAdmonition(attributes)).isTrue();
    }
}
