package org.zanata.webtrans.client.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TextFormatUtilTest {

    @Test
    public void testFormatPercentage() throws Exception {
        assertThat(TextFormatUtil.formatPercentage(55.8)).isEqualTo("55.0");
    }

    @Test
    public void testFormatHours() throws Exception {
        assertThat(TextFormatUtil.formatHours(5.812)).isEqualTo("5.82");
    }
}
