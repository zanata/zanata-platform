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

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import static org.owasp.html.Sanitizers.BLOCKS;
import static org.owasp.html.Sanitizers.FORMATTING;
import static org.owasp.html.Sanitizers.IMAGES;
import static org.owasp.html.Sanitizers.LINKS;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class HtmlUtil {
    // add em as workaround for https://code.google.com/p/owasp-java-html-sanitizer/issues/detail?id=31
    public static final PolicyFactory MORE_FORMATTING = new HtmlPolicyBuilder()
            .allowElements("em", "pre",
                    "table", "th", "tr", "td",
                    "caption", "colgroup", "col",
                    "thead", "tbody", "tfoot").toFactory();
    // Don't allow CSS styles, scripts, etc
    public static final PolicyFactory SANITIZER =
            BLOCKS.and(FORMATTING).and(MORE_FORMATTING).and(IMAGES).and(LINKS);

    /**
     * Converts HTML to plain text.  'br' tags become newlines, and URLs in
     * links are inserted after the text of the link.
     * @param html
     * @return
     */
    public static String htmlToText(String html) {
        Source htmlSource = new Source(html);
        Segment htmlSeg = new Segment(htmlSource, 0, htmlSource.length());
        Renderer htmlRend = new Renderer(htmlSeg);
        return htmlRend.toString();
    }
}
