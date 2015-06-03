package org.zanata.webtrans.client.util;


import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TextFormatUtilTest {

    @Test
    public void testFormatPercentage() throws Exception {
        assertThat(TextFormatUtil.formatPercentage(55.8)).isEqualTo("55.0");
        Assertions.assertThat(TextFormatUtil.formatPercentage(80.01)).isEqualTo("80.0");
        Assertions.assertThat(TextFormatUtil.formatPercentage(80.05)).isEqualTo("80.0");
        Assertions.assertThat(TextFormatUtil.formatPercentage(80.15)).isEqualTo("80.0");
        Assertions.assertThat(TextFormatUtil.formatPercentage(80.55)).isEqualTo("80.0");
    }

    @Test
    public void testFormatHours() throws Exception {
        assertThat(TextFormatUtil.formatHours(5.812)).isEqualTo("5.82");
        Assertions.assertThat(TextFormatUtil.formatHours(12.01)).isEqualTo("12.01");
        Assertions.assertThat(TextFormatUtil.formatHours(12.05)).isEqualTo("12.05");
        Assertions.assertThat(TextFormatUtil.formatHours(12.15)).isEqualTo("12.15");
        Assertions.assertThat(TextFormatUtil.formatHours(12.55)).isEqualTo("12.55");
    }
}
