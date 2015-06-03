/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.util;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.HtmlUtil.*;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class HtmlUtilTest {

    @Test
    public void sanitisePlainText() {
        String input = "some text";
        String actual = SANITIZER.sanitize(input);
        assertThat(actual).isEqualTo(input);
    }

    @Test
    public void sanitiseSafeHtml() {
        String input = "<p>This is <b>meant</b> to <br />contain a <a href=\"http://example.com/\" rel=\"nofollow\">link</a>.</p>";
        String actual = SANITIZER.sanitize(input);
        assertThat(actual).isEqualTo(input);
    }

    // NB: This is not meant to be a comprehensive test of the OWASP HTML
    // Sanitizer; it's just a smoke test to make sure it's switched on.
    @Test
    public void sanitiseUnsafeHtml() {
        String input = "<p>This HTML contains a script <script></script> tag.</p>";
        String expected = "<p>This HTML contains a script  tag.</p>";
        String actual = SANITIZER.sanitize(input);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testHtmlToString() {
        String input = "<p>This is <b>meant</b> to <br/>contain a " +
                "<a href='http://example.com/'>link</a>.</p>";
        String actual = htmlToText(input);
        assertThat(actual).isEqualTo("This is meant to\r\n" +
                "contain a link <http://example.com/> .");
    }

}
