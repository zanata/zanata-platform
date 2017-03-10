/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.log4j;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.zanata.servlet.MDCInsertingServletFilter;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.TimeZone;
import static org.apache.log4j.helpers.Transform.escapeTags;

/**
 * Extension of Log4J's HTML layout.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataHTMLLayout extends HTMLLayout {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ZanataHTMLLayout.class);

    private final String build;

    public ZanataHTMLLayout(String build) {
        this.build = build;
    }

    @Override
    public String format(LoggingEvent event) {
        StringBuilder builder = new StringBuilder();
        String timestamp = String.format("%tFT%<tTZ",
                Calendar.getInstance(TimeZone.getTimeZone("Z")));
        addValue(builder, "Time", timestamp);
        addValue(builder, "Build", build);
        // We could ask jboss-logmanager's LoggingEvent for the ExtLogRecord,
        // and then call getMDCCopy().keySet() to get a complete list of
        // available MDC keys, but this would tie us to jboss-logmanager.
        for (String key : MDCInsertingServletFilter.getMDCKeys()) {
            addValue(builder, key, event.getMDC(key));
        }
        builder.append(super.format(event));
        String html = builder.toString();
        log.debug(html);
        return html;
    }

    private void addValue(StringBuilder sbuf, String key,
            @Nullable Object value) {
        if (value != null) {
            sbuf.append("<tr><td colspan=\"6\">");
            sbuf.append(key);
            sbuf.append(": ");
            sbuf.append(escapeTags(value.toString()));
            sbuf.append("</td></tr>");
            sbuf.append(Layout.LINE_SEP);
        }
    }
}
