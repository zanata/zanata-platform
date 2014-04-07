package org.zanata.search;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.dao.TextFlowDAO;
import org.zanata.webtrans.shared.model.ContentStateGroup;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FilterConstraintToModalNavigationQueryTest {

    @Test
    public void canBuildAcceptAllQuery() {
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(ContentStateGroup
                        .builder().addAll().build(), "tft");
        assertThat("Conditional that accepts all should be '1'",
                contentStateCondition, is("1"));
    }

    @Test
    public void canBuildAcceptAllQueryWhenNoStatesSelected() {
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(ContentStateGroup
                        .builder().removeAll().build(), "tft");
        assertThat("Conditional that accepts none should be '0'",
                contentStateCondition, is("0"));
    }

    @Test
    public void canBuildNewOnlyConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeNew(true)
                        .build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition,
                is("(tft.state=0 or tft.state is null)"));
    }

    @Test
    public void canBuildFuzzyOnlyConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeFuzzy(true)
                        .build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition, is("(tft.state=1)"));
    }

    @Test
    public void canBuildTranslatedOnlyConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeTranslated(true)
                        .build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition, is("(tft.state=2)"));
    }

    @Test
    public void canBuildApprovedOnlyConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeApproved(true)
                        .build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition, is("(tft.state=3)"));
    }

    @Test
    public void canBuildRejectedOnlyConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeRejected(true)
                        .build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition, is("(tft.state=4)"));
    }

    @Test
    public void canBuildNewAndFuzzyConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeNew(true)
                        .includeFuzzy(true).build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition,
                is("(tft.state=0 or tft.state is null or tft.state=1)"));
    }

    @Test
    public void canBuildNewAndTranslatedConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeNew(true)
                        .includeTranslated(true).build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition,
                is("(tft.state=0 or tft.state is null or tft.state=2)"));
    }

    @Test
    public void canBuildFuzzyAndTranslatedConditional() {
        ContentStateGroup contentStates =
                ContentStateGroup.builder().removeAll().includeFuzzy(true)
                        .includeTranslated(true).build();
        String contentStateCondition =
                FilterConstraintToModalNavigationQuery.buildContentStateCondition(contentStates, "tft");
        assertThat(contentStateCondition, is("(tft.state=1 or tft.state=2)"));
    }

    @Test
    public void canBuildSearchQuery() {
        // no search term
        assertThat(FilterConstraintToModalNavigationQuery.buildSearchCondition(null, "tft"),
                Matchers.equalTo("1"));
        assertThat(FilterConstraintToModalNavigationQuery.buildSearchCondition("", "tft"),
                Matchers.equalTo("1"));

        // with search term
        assertThat(
                FilterConstraintToModalNavigationQuery.buildSearchCondition("a", "tft"),
                Matchers.equalTo("(lower(tft.content0) LIKE :searchstringlowercase or lower(tft.content1) LIKE :searchstringlowercase or lower(tft.content2) LIKE :searchstringlowercase or lower(tft.content3) LIKE :searchstringlowercase or lower(tft.content4) LIKE :searchstringlowercase or lower(tft.content5) LIKE :searchstringlowercase)"));
        assertThat(
                FilterConstraintToModalNavigationQuery.buildSearchCondition("A", "tft"),
                Matchers.equalTo("(lower(tft.content0) LIKE :searchstringlowercase or lower(tft.content1) LIKE :searchstringlowercase or lower(tft.content2) LIKE :searchstringlowercase or lower(tft.content3) LIKE :searchstringlowercase or lower(tft.content4) LIKE :searchstringlowercase or lower(tft.content5) LIKE :searchstringlowercase)"));

    }
}
