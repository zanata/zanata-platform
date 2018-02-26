package org.zanata.rest.editor.dto;

import org.junit.Test;
import org.zanata.common.ContentState;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
public class ReviewDataTest {

    @Test
    public void transUnitIdTest() {
        Long id = 10L;
        ReviewData data = new ReviewData();
        data.setTransUnitId(id);
        assertThat(data.getTransUnitId()).isEqualTo(id);
    }

    @Test
    public void revisionTest() {
        int revision = 10;
        ReviewData data = new ReviewData();
        data.setRevision(revision);
        assertThat(data.getRevision()).isEqualTo(revision);
    }

    @Test
    public void commentTest() {
        String comment = "comment";
        ReviewData data = new ReviewData();
        data.setComment(comment);
        assertThat(data.getComment()).isEqualTo(comment);
    }

    @Test
    public void criteriaIdTest() {
        Long id = 10L;
        ReviewData data = new ReviewData();
        data.setReviewCriteriaId(id);
        assertThat(data.getReviewCriteriaId()).isEqualTo(id);
    }

    @Test
    public void statusTest() {
        ContentState status = ContentState.Approved;
        ReviewData data = new ReviewData();
        data.setStatus(status);
        assertThat(data.getStatus()).isEqualTo(status);
    }

    @Test
    public void equalTest() {
        ContentState status = ContentState.Approved;
        ReviewData data = new ReviewData();
        data.setStatus(status);
        data.setReviewCriteriaId(1L);

        ReviewData data2 = new ReviewData();
        data2.setStatus(status);
        data2.setReviewCriteriaId(1L);

        assertThat(data).isEqualTo(data2);
        assertThat(data.hashCode()).isEqualTo(data2.hashCode());
    }
}
