package org.zanata.webtrans.client.util;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.zanata.common.ContentState.*;
import static org.zanata.webtrans.client.util.ContentStateToStyleUtil.stateToStyle;

public class ContentStateToStyleUtilTest {

    @Test
    public void testStateToStyle() throws Exception {
        assertThat(stateToStyle(New)).contains("neutral");
        assertThat(stateToStyle(NeedReview)).contains("unsure");
        assertThat(stateToStyle(Translated)).contains("success");
        assertThat(stateToStyle(Approved)).contains("highlight");
        assertThat(stateToStyle(Rejected)).contains("danger");
    }

    @Test
    public void testStateToExtraStyle() throws Exception {
        String initial = "initial";
        assertThat(stateToStyle(New, initial)).isEqualTo(initial);
        assertThat(stateToStyle(NeedReview, initial)).contains("FuzzyStateDecoration");
        assertThat(stateToStyle(Translated, initial)).contains("TranslatedStateDecoration");
        assertThat(stateToStyle(Approved, initial)).contains("ApprovedStateDecoration");
        assertThat(stateToStyle(Rejected, initial)).contains("RejectedStateDecoration");
    }
}
