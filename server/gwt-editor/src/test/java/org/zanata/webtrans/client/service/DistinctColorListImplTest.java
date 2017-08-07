package org.zanata.webtrans.client.service;

import org.junit.Before;
import org.junit.Test;
import org.zanata.webtrans.shared.auth.EditorClientId;
import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DistinctColorListImplTest {
    private static int counter = 0;
    private DistinctColorListImpl distinctColor;

    @Before
    public void beforeMethod() {
        distinctColor =
                new DistinctColorListImpl(Lists.newArrayList("red", "blue",
                        "green"));
    }

    private static EditorClientId newEditorClientId() {
        counter++;
        return new EditorClientId(String.valueOf(counter), counter);
    }

    @Test
    public void canGetNextColor() {
        assertThat(distinctColor.getOrCreateColor(newEditorClientId()))
                .isEqualTo("red");
        assertThat(distinctColor.getOrCreateColor(newEditorClientId()))
                .isEqualTo("blue");
        assertThat(distinctColor.getOrCreateColor(newEditorClientId()))
                .isEqualTo("green");
        assertThat(distinctColor.getOrCreateColor(newEditorClientId()))
                .isEqualTo("red");
    }

    @Test
    public void willReuseColorForSameEditorClientId() {
        EditorClientId editorClientId = newEditorClientId();
        String color = distinctColor.getOrCreateColor(editorClientId);
        String sameColor = distinctColor.getOrCreateColor(editorClientId);

        assertThat(sameColor).isEqualTo(color);
    }

    @Test
    public void canReleaseColor() {
        EditorClientId editorClientId = newEditorClientId();
        String color = distinctColor.getOrCreateColor(editorClientId);
        distinctColor.releaseColor(editorClientId);
        String newColor = distinctColor.getOrCreateColor(editorClientId);

        assertThat(newColor).isNotEqualTo(color);
    }
}
