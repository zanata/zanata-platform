package org.zanata.file;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(id.getDocId()).isEqualTo(DOCUMENT_ID);
    }

    @Test
    public void getIterationSlug() {
        assertThat(id.getVersionSlug()).isEqualTo(VERSION_SLUG);
    }

    @Test
    public void getProjectSlug() {
        assertThat(id.getProjectSlug()).isEqualTo(PROJECT_SLUG);
    }

    @Test
    public void equalsIsSymmetric() {
        GlobalDocumentId sameId = newBasicInstance();
        assertThat(sameId).isEqualTo(id);
    }

    @Test
    public void sameHashForEqualObjects() {
        GlobalDocumentId sameId = newBasicInstance();
        assertThat(sameId.hashCode()).isEqualTo(id.hashCode());
    }
}
