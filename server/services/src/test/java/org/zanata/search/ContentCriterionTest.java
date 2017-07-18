package org.zanata.search;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ContentCriterionTest {
    private ContentCriterion criterion = new ContentCriterion(2);

    @Test
    public void canBuildWithAlias() {
        String hql =
                criterion.withEntityAlias("tf").withCaseSensitive(true)
                        .contentsCriterionAsString();

        assertThat(
                hql,
                Matchers.equalTo("(tf.content0 like :SearchString OR tf.content1 like :SearchString)"));
    }

    @Test
    public void canBuildWithoutAlias() {
        String hql =
                criterion.withCaseSensitive(true).contentsCriterionAsString();

        assertThat(
                hql,
                Matchers.equalTo("(content0 like :SearchString OR content1 like :SearchString)"));
    }

    @Test
    public void caseInsensitive() {
        String hql = criterion.contentsCriterionAsString();

        assertThat(
                hql,
                Matchers.equalTo("(lower(content0) like :SearchString OR lower(content1) like :SearchString)"));
    }
}
