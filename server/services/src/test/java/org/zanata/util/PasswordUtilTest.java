package org.zanata.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordUtilTest {

    @Test
    @Deprecated
    public void testGenerateSaltedHash() throws Exception {

        assertThat(PasswordUtil.generateSaltedHash("admin", "admin"))
                .isEqualTo("Eyox7xbNQ09MkIfRyH+rjg==");
        assertThat(PasswordUtil.generateSaltedHash("translator", "translator"))
                .isEqualTo("Fr5JHlcaEqKLSHjnBm4gXg==");
        assertThat(PasswordUtil.generateSaltedHash("glossarist", "glossarist"))
                .isEqualTo("fRIeiPDPlSMtHbBNoqDjNQ==");
        assertThat(
                PasswordUtil.generateSaltedHash("glossaryadmin",
                        "glossaryadmin")).isEqualTo("/W0YpteXk+WtymQ7H84kPQ==");

        assertThat(PasswordUtil.generateSaltedHash("user", "$dos-3boD"))
                .isEqualTo("llwZKXl4KH5lb85UkcSCgA==");
    }
}
