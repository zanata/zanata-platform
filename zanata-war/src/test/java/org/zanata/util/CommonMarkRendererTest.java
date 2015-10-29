/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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


import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class CommonMarkRendererTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    private CommonMarkRenderer renderer = new CommonMarkRenderer();

    @Test
    public void testRenderToHtmlSafe() throws Exception {
        String source = "This text contains an *unsafe* <script>script</script> element.";
        String expected = "<p>This text contains an <em>unsafe</em>  element.</p>\n";
        String rendered = renderer.renderToHtmlSafe(source);
        assertThat(rendered).isEqualTo(expected);
    }

    @Test
    // 10,000 iterations should run in a few seconds
    // if you reuse the ScriptEngine and CompiledScript correctly
//    @Repeat(times = 10_000)
    public void testRenderToHtmlUnsafe() throws Exception {
        String source = "This text contains an *unsafe* <script>script</script> element.";
        String expected = "<p>This text contains an <em>unsafe</em> <script>script</script> element.</p>\n";
        String rendered = renderer.renderToHtmlUnsafe(source);
        assertThat(rendered).isEqualTo(expected);
    }

    // This test fails with commonmark.js 0.18.1 as minified by
    // jscompress.com (UglifyJS v1):
    @Test
    public void testRenderAmpersandInsideCodeFence() throws Exception {
        String source = "```\n" +
                "for (int i=0; i&lt;100; i++) {\n" +
                "}\n" +
                "```";
        String expected = "<pre><code>for (int i=0; i&amp;lt;100; i++) {\n" +
                "}\n" +
                "</code></pre>\n";
        String rendered = renderer.renderToHtmlUnsafe(source);
        assertThat(rendered).isEqualTo(expected);
    }
}
