package org.zanata.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ZanataHTMLLayoutTest {

    @Test
    public void testFormat() {
        String buildInfo = "Build information of current Zanata instance";
        ZanataHTMLLayout zanataHTMLLayout = new ZanataHTMLLayout(buildInfo);
        LoggingEvent event = Mockito.mock(LoggingEvent.class);
        when(event.getLevel()).thenReturn(Level.DEBUG);
        String html = zanataHTMLLayout.format(event);

        assertThat(html).contains(buildInfo).contains(Level.DEBUG.toString());
    }
}
