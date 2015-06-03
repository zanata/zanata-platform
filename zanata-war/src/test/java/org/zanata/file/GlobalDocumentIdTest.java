package org.zanata.file;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class GlobalDocumentIdTest {

    private static final String PROJECT_SLUG = "project";
    private static final String VERSION_SLUG = "version";
    private static final String DOCUMENT_ID = "document";

    private GlobalDocumentId id;

    @Before
    public void setup() {
        id = newBasicInstance();
    }

    private GlobalDocumentId newBasicInstance() {
        return new GlobalDocumentId(PROJECT_SLUG, VERSION_SLUG, DOCUMENT_ID);
    }

    @Test
    public void getDocId() {
        assertThat(id.getDocId(), is(DOCUMENT_ID));
    }

    @Test
    public void getIterationSlug() {
        assertThat(id.getVersionSlug(), is(VERSION_SLUG));
    }

    @Test
    public void getProjectSlug() {
        assertThat(id.getProjectSlug(), is(PROJECT_SLUG));
    }

    @Test
    public void equalsIsReflexive() {
        assertThat(id, equalTo(id));
    }

    @Test
    public void equalsIsSymmetric() {
        GlobalDocumentId sameId = newBasicInstance();
        assertThat(sameId, equalTo(id));
        assertThat(id, equalTo(sameId));
    }

    @Test
    public void sameHashForEqualObjects() {
        GlobalDocumentId sameId = newBasicInstance();
        assertThat(sameId.hashCode(), is(id.hashCode()));
    }
}
