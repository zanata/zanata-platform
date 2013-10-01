package org.zanata.webtrans.shared.util;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.zanata.webtrans.shared.util.StringNotEmptyPredicate.INSTANCE;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class StringNotEmptyPredicateTest {
    @Test
    public void testApply() throws Exception {
        assertThat(INSTANCE.apply(""), Matchers.equalTo(false));
        assertThat(INSTANCE.apply(null), Matchers.equalTo(false));
        assertThat(INSTANCE.apply(" "), Matchers.equalTo(true));
        assertThat(INSTANCE.apply("a"), Matchers.equalTo(true));
    }
}
