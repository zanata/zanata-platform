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

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class CommonMarkRendererTest {

    private CommonMarkRenderer renderer = new CommonMarkRenderer();

    @Test
    public void testRenderToHtmlSafe() throws Exception {
        String source = "This text contains an *unsafe* <script>script</script> element.";
        String expected = "<p>This text contains an <em>unsafe</em>  element.</p>\n";
        String rendered = renderer.renderToHtmlSafe(source);
        assertThat(rendered).isEqualTo(expected);
    }

    @Test
    public void testRenderToHtmlUnsafe() throws Exception {
        String source = "This text contains an *unsafe* <script>script</script> element.";
        String expected = "<p>This text contains an <em>unsafe</em> <script>script</script> element.</p>\n";
        String rendered = renderer.renderToHtmlUnsafe(source);
        assertThat(rendered).isEqualTo(expected);
    }
}
