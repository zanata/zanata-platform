package org.zanata.search;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ContentCriterionTest {
    private ContentCriterion criterion = new ContentCriterion(2);

    @Test
    public void canBuildWithAlias() {
        String hql = criterion.contentsCriterionAsString("tf", true,
                FilterConstraintToQuery.Parameters.searchString
                        .placeHolder());

        assertThat(hql, Matchers.equalTo("(tf.content0 like :searchTerm OR tf.content1 like :searchTerm)"));
    }

    @Test
    public void canBuildWithoutAlias() {
        String hql = criterion.contentsCriterionAsString(null, true,
                FilterConstraintToQuery.Parameters.searchString
                        .placeHolder());

        assertThat(hql, Matchers.equalTo("(content0 like :searchTerm OR content1 like :searchTerm)"));
    }

    @Test
    public void caseInsensitive() {
        String hql = criterion.contentsCriterionAsString("", false,
                FilterConstraintToQuery.Parameters.searchString
                        .placeHolder());

        assertThat(hql, Matchers.equalTo("(lower(content0) like :searchTerm OR lower(content1) like :searchTerm)"));
    }
}
