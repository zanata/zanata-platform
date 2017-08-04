package org.zanata.webtrans.shared.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.webtrans.shared.util.StringNotEmptyPredicate.INSTANCE;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class StringNotEmptyPredicateTest {
    @Test
    public void testApply() throws Exception {
        assertThat(INSTANCE.apply("")).isFalse();
        assertThat(INSTANCE.apply(null)).isFalse();
        assertThat(INSTANCE.apply(" ")).isTrue();
        assertThat(INSTANCE.apply("a")).isTrue();
    }
}
